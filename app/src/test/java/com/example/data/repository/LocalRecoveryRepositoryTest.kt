package com.example.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CustomReward
import com.example.data.model.DailyCheckIn
import com.example.data.model.Goal
import com.example.data.model.Reward
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], manifest = Config.NONE)
class LocalRecoveryRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: LocalRecoveryRepository
    private val testUserId = "test_user_42"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        com.example.data.local.LocalRecoveryDatabase.getInstance(context).resetAllData(testUserId)
        repository = LocalRecoveryRepository(context)
    }

    @Test
    fun profileCreationAndObservation_succeeds() = runBlocking {
        val createResult = repository.createProfile(testUserId, "2026-11-01", "Substances / Alcohol")
        assertTrue("Profile creation should succeed", createResult.isSuccess)

        val profile = repository.observeProfile(testUserId).first { it != null }
        assertNotNull(profile)
        assertEquals(testUserId, profile?.userId)
        assertEquals("Substances / Alcohol", profile?.substanceName)
        assertEquals("2026-11-01", profile?.soberStartDate)

        val awardResult = repository.awardPoints(testUserId, 50L)
        assertTrue("Award points should succeed", awardResult.isSuccess)

        val updatedProfile = repository.getProfile(testUserId)
        assertEquals(50L, updatedProfile?.points)
    }

    @Test
    fun goalLifecycle_succeeds() = runBlocking {
        val goalId = "goal_" + UUID.randomUUID().toString().take(8)
        val goal = Goal(
            id = goalId,
            userId = testUserId,
            title = "Daily Meditation",
            period = "daily",
            category = "mindfulness",
            targetCount = 1L,
            currentProgress = 0L,
            isCompleted = false
        )

        val createResult = repository.createGoal(testUserId, goal)
        assertTrue(createResult.isSuccess)

        val goals = repository.observeGoals(testUserId).first { list -> list.any { it.id == goalId } }
        val found = goals.find { it.id == goalId }
        assertNotNull(found)
        assertEquals("Daily Meditation", found?.title)
        assertFalse(found!!.isCompleted)

        val updateResult = repository.updateGoalProgress(testUserId, goalId, 1L, true)
        assertTrue(updateResult.isSuccess)

        val updatedGoals = repository.observeGoals(testUserId).first { list -> list.any { it.id == goalId && it.isCompleted } }
        assertTrue(updatedGoals.find { it.id == goalId }!!.isCompleted)

        val deleteResult = repository.deleteGoal(testUserId, goalId)
        assertTrue(deleteResult.isSuccess)
    }

    @Test
    fun checkInLoggingAndRetrieval_succeeds() = runBlocking {
        val checkIn = DailyCheckIn(
            id = "2026-11-12",
            userId = testUserId,
            date = "2026-11-12",
            status = "sober",
            cravingLevel = 1L,
            mood = "great",
            notes = "Feeling energetic and clear-headed!",
            triggers = listOf("social", "stress")
        )

        val logResult = repository.logCheckIn(testUserId, checkIn)
        assertTrue(logResult.isSuccess)

        val checkIns = repository.observeCheckIns(testUserId).first { list -> list.any { it.date == "2026-11-12" } }
        val retrieved = checkIns.find { it.date == "2026-11-12" }
        assertNotNull(retrieved)
        assertEquals("sober", retrieved?.status)
        assertEquals(1L, retrieved?.cravingLevel)
        assertEquals(2, retrieved?.triggers?.size)
    }

    @Test
    fun customRewardAndRedemption_succeeds() = runBlocking {
        repository.createProfile(testUserId, "2026-11-01", "Alcohol")
        repository.awardPoints(testUserId, 100L)

        val reward = CustomReward(
            id = "rew_1",
            userId = testUserId,
            title = "Special Ice Cream",
            costPoints = 40L
        )

        val createResult = repository.createCustomReward(testUserId, reward)
        assertTrue(createResult.isSuccess)

        val customRewards = repository.observeCustomRewards(testUserId).first { list -> list.any { it.id == "rew_1" } }
        assertEquals(1, customRewards.size)
        assertFalse(customRewards[0].isRedeemed)

        val redeemResult = repository.redeemCustomReward(testUserId, "rew_1", 40L)
        assertTrue(redeemResult.isSuccess)

        val profileAfter = repository.getProfile(testUserId)
        assertEquals(60L, profileAfter?.points)
    }
}
