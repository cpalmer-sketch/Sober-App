package com.example.data.repository

import android.content.Context
import com.example.data.local.LocalRecoveryDatabase
import com.example.data.model.CustomReward
import com.example.data.model.DailyCheckIn
import com.example.data.model.Goal
import com.example.data.model.Reward
import com.example.data.model.UserProfile
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class LocalRecoveryRepository(context: Context) : RecoveryRepository {

    private val db = LocalRecoveryDatabase.getInstance(context)

    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    private val goalsFlow = MutableStateFlow<List<Goal>>(emptyList())
    private val checkInsFlow = MutableStateFlow<List<DailyCheckIn>>(emptyList())
    private val rewardsFlow = MutableStateFlow<List<Reward>>(emptyList())
    private val customRewardsFlow = MutableStateFlow<List<CustomReward>>(emptyList())

    override fun observeProfile(userId: String): Flow<UserProfile?> {
        profileFlow.value = db.getUserProfile(userId)
        return profileFlow.asStateFlow()
    }

    override suspend fun getProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        db.getUserProfile(userId)
    }

    override suspend fun createProfile(
        userId: String,
        soberStartDate: String,
        substanceName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = db.getUserProfile(userId)
            val profile = if (existing != null) {
                existing.copy(
                    soberStartDate = soberStartDate,
                    substanceName = substanceName,
                    updatedAt = Timestamp.now()
                )
            } else {
                UserProfile(
                    userId = userId,
                    soberStartDate = soberStartDate,
                    substanceName = substanceName,
                    points = 0L,
                    currentStreakDays = 0L,
                    longestStreakDays = 0L,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
            }
            db.upsertUserProfile(profile)
            profileFlow.value = profile
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(userId: String, profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updated = profile.copy(updatedAt = Timestamp.now())
            db.upsertUserProfile(updated)
            profileFlow.value = updated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun awardPoints(userId: String, pointsToAdd: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.addPoints(userId, pointsToAdd)
            profileFlow.value = db.getUserProfile(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeGoals(userId: String): Flow<List<Goal>> {
        goalsFlow.value = db.getGoals(userId)
        return goalsFlow.asStateFlow()
    }

    override suspend fun createGoal(userId: String, goal: Goal): Result<String> = withContext(Dispatchers.IO) {
        try {
            val goalId = if (goal.id.isNotBlank()) goal.id else "goal_${UUID.randomUUID().toString().take(8)}"
            val toSave = goal.copy(
                id = goalId,
                userId = userId,
                createdAt = goal.createdAt ?: Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            db.upsertGoal(toSave)
            goalsFlow.value = db.getGoals(userId)
            Result.success(goalId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGoalProgress(
        userId: String,
        goalId: String,
        newProgress: Long,
        isCompleted: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.updateGoalProgress(goalId, newProgress, isCompleted)
            goalsFlow.value = db.getGoals(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGoal(userId: String, goalId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.deleteGoal(goalId)
            goalsFlow.value = db.getGoals(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeCheckIns(userId: String): Flow<List<DailyCheckIn>> {
        checkInsFlow.value = db.getCheckIns(userId)
        return checkInsFlow.asStateFlow()
    }

    override suspend fun logCheckIn(userId: String, checkIn: DailyCheckIn): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docId = if (checkIn.id.isNotBlank()) checkIn.id else checkIn.date
            val toSave = checkIn.copy(
                id = docId,
                userId = userId,
                createdAt = checkIn.createdAt ?: Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            db.upsertCheckIn(toSave)
            checkInsFlow.value = db.getCheckIns(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeRewards(userId: String): Flow<List<Reward>> {
        rewardsFlow.value = db.getRewards(userId)
        return rewardsFlow.asStateFlow()
    }

    override suspend fun unlockReward(userId: String, reward: Reward): Result<String> = withContext(Dispatchers.IO) {
        try {
            val rewardId = if (reward.id.isNotBlank()) reward.id else "rew_${UUID.randomUUID().toString().take(8)}"
            val toSave = reward.copy(
                id = rewardId,
                userId = userId,
                createdAt = reward.createdAt ?: Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            db.upsertReward(toSave)
            if (reward.pointsAwarded > 0) {
                db.addPoints(userId, reward.pointsAwarded)
                profileFlow.value = db.getUserProfile(userId)
            }
            rewardsFlow.value = db.getRewards(userId)
            Result.success(rewardId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeCustomRewards(userId: String): Flow<List<CustomReward>> {
        customRewardsFlow.value = db.getCustomRewards(userId)
        return customRewardsFlow.asStateFlow()
    }

    override suspend fun createCustomReward(userId: String, reward: CustomReward): Result<String> = withContext(Dispatchers.IO) {
        try {
            val rewardId = if (reward.id.isNotBlank()) reward.id else "creward_${UUID.randomUUID().toString().take(8)}"
            val toSave = reward.copy(
                id = rewardId,
                userId = userId,
                createdAt = reward.createdAt ?: Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            db.upsertCustomReward(toSave)
            customRewardsFlow.value = db.getCustomRewards(userId)
            Result.success(rewardId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun redeemCustomReward(
        userId: String,
        rewardId: String,
        costPoints: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.redeemCustomReward(rewardId)
            db.addPoints(userId, -costPoints)
            customRewardsFlow.value = db.getCustomRewards(userId)
            profileFlow.value = db.getUserProfile(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomReward(userId: String, rewardId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.deleteCustomReward(rewardId)
            customRewardsFlow.value = db.getCustomRewards(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
