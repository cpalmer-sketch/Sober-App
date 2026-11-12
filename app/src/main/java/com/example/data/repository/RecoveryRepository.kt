package com.example.data.repository

import android.content.Context
import com.example.data.model.CustomReward
import com.example.data.model.DailyCheckIn
import com.example.data.model.Goal
import com.example.data.model.Reward
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

interface RecoveryRepository {

    // --- USER PROFILE & STREAKS ---
    fun observeProfile(userId: String): Flow<UserProfile?>
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun createProfile(userId: String, soberStartDate: String, substanceName: String): Result<Unit>
    suspend fun updateProfile(userId: String, profile: UserProfile): Result<Unit>
    suspend fun awardPoints(userId: String, pointsToAdd: Long): Result<Unit>

    // --- GOALS ---
    fun observeGoals(userId: String): Flow<List<Goal>>
    suspend fun createGoal(userId: String, goal: Goal): Result<String>
    suspend fun updateGoalProgress(userId: String, goalId: String, newProgress: Long, isCompleted: Boolean): Result<Unit>
    suspend fun deleteGoal(userId: String, goalId: String): Result<Unit>

    // --- DAILY CHECK-INS ---
    fun observeCheckIns(userId: String): Flow<List<DailyCheckIn>>
    suspend fun logCheckIn(userId: String, checkIn: DailyCheckIn): Result<Unit>

    // --- REWARDS & BADGES ---
    fun observeRewards(userId: String): Flow<List<Reward>>
    suspend fun unlockReward(userId: String, reward: Reward): Result<String>

    // --- CUSTOM DOPAMINE REWARDS ---
    fun observeCustomRewards(userId: String): Flow<List<CustomReward>>
    suspend fun createCustomReward(userId: String, reward: CustomReward): Result<String>
    suspend fun redeemCustomReward(userId: String, rewardId: String, costPoints: Long): Result<Unit>
    suspend fun deleteCustomReward(userId: String, rewardId: String): Result<Unit>

    companion object {
        operator fun invoke(db: FirebaseFirestore): RecoveryRepository {
            return FirestoreRecoveryRepository(db)
        }

        operator fun invoke(context: Context): RecoveryRepository {
            return LocalRecoveryRepository(context)
        }

        fun local(context: Context): RecoveryRepository {
            return LocalRecoveryRepository(context)
        }

        fun firestore(db: FirebaseFirestore): RecoveryRepository {
            return FirestoreRecoveryRepository(db)
        }
    }
}
