package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.DailyCheckIn
import com.example.data.model.Goal
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class RecoveryRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createAndObserveProfile_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = RecoveryRepository(firestore)

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createProfile(uid, "2026-11-01", "Substances / Alcohol")
    }
    assertTrue("Create profile failed: ${result.exceptionOrNull()}", result.isSuccess)

    val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.observeProfile(uid).first { it != null }
    }
    assertNotNull(profile)
    assertEquals(uid, profile?.userId)
    assertEquals("Substances / Alcohol", profile?.substanceName)
  }

  @Test
  fun createGoalAndObserve_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = RecoveryRepository(firestore)
    val goalId = "goal_" + UUID.randomUUID().toString().take(8)

    val goal = Goal(
      id = goalId,
      userId = uid,
      title = "Surfe Urges for 15 minutes",
      period = "daily",
      category = "mindfulness",
      targetCount = 1L,
      currentProgress = 0L,
      isCompleted = false
    )

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createGoal(uid, goal)
    }
    assertTrue("Create goal should succeed", result.isSuccess)

    val goals = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.observeGoals(uid).first { list -> list.any { it.id == goalId } }
    }
    assertTrue("Should contain created goal", goals.any { it.id == goalId })

    val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.updateGoalProgress(uid, goalId, 1L, true)
    }
    assertTrue("Update goal should succeed", updateResult.isSuccess)
  }

  @Test
  fun logCheckIn_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = RecoveryRepository(firestore)

    val checkIn = DailyCheckIn(
      id = "2026-11-11",
      userId = uid,
      date = "2026-11-11",
      status = "sober",
      cravingLevel = 2L,
      mood = "great",
      notes = "Focused on deep breathing and stayed clean today!",
      triggers = listOf("stress")
    )

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.logCheckIn(uid, checkIn)
    }
    assertTrue("Log check in failed: ${result.exceptionOrNull()?.message}", result.isSuccess)

    val checkIns = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.observeCheckIns(uid).first { list -> list.any { it.date == "2026-11-11" } }
    }
    assertTrue("Check in should be emitted", checkIns.any { it.date == "2026-11-11" })
  }

  @Test
  fun crossUserAccess_bobCannotReadAliceGoals() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = RecoveryRepository(firestore)
    val goalId = "alice_goal_" + UUID.randomUUID().toString().take(8)

    val goal = Goal(
      id = goalId,
      userId = aliceUid,
      title = "Alice private goal",
      period = "daily",
      category = "health",
      targetCount = 1L,
      currentProgress = 0L,
      isCompleted = false
    )
    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createGoal(aliceUid, goal)
    }

    // Now sign in as Bob and attempt to observe Alice's goals
    signInTestUser(BOB_EMAIL)
    val bobRepo = RecoveryRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        bobRepo.observeGoals(aliceUid).first()
      }
      fail("Expected PERMISSION_DENIED when Bob tries to read Alice's goals")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreException)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }

  @Test
  fun unauthenticatedUser_cannotObserveGoals() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = RecoveryRepository(firestore)
    val goalId = "alice_goal_" + UUID.randomUUID().toString().take(8)

    val goal = Goal(
      id = goalId,
      userId = aliceUid,
      title = "Alice goal",
      period = "daily",
      targetCount = 1L
    )
    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createGoal(aliceUid, goal)
    }

    // Sign out
    auth.signOut()
    val repo = RecoveryRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeGoals(aliceUid).first()
      }
      fail("Expected PERMISSION_DENIED when unauthenticated")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreException)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@test.com"
    const val BOB_EMAIL = "bob@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
