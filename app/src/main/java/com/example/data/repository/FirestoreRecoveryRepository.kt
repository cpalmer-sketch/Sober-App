package com.example.data.repository

import com.example.data.model.CustomReward
import com.example.data.model.DailyCheckIn
import com.example.data.model.Goal
import com.example.data.model.Reward
import com.example.data.model.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRecoveryRepository(
    private val db: FirebaseFirestore
) : RecoveryRepository {

    // --- USER PROFILE & STREAKS ---

    override fun observeProfile(userId: String): Flow<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        }
    }

    override suspend fun getProfile(userId: String): UserProfile? {
        val docRef = db.collection("users").document(userId)
        val snapshot = docRef.get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
            null
        }
    }

    override suspend fun createProfile(userId: String, soberStartDate: String, substanceName: String): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val payload = mapOf(
                    "soberStartDate" to soberStartDate,
                    "substanceName" to substanceName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(payload).await()
            } else {
                val payload = mapOf(
                    "userId" to userId,
                    "soberStartDate" to soberStartDate,
                    "substanceName" to substanceName,
                    "points" to 0L,
                    "currentStreakDays" to 0L,
                    "longestStreakDays" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(userId: String, profile: UserProfile): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        val payload = mapOf(
            "soberStartDate" to profile.soberStartDate,
            "substanceName" to profile.substanceName,
            "points" to profile.points,
            "currentStreakDays" to profile.currentStreakDays,
            "longestStreakDays" to profile.longestStreakDays,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun awardPoints(userId: String, pointsToAdd: Long): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        val payload = mapOf(
            "points" to FieldValue.increment(pointsToAdd),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- GOALS ---

    override fun observeGoals(userId: String): Flow<List<Goal>> {
        val collRef = db.collection("users").document(userId).collection("goals")
        return collRef.snapshots().map { snapshot ->
            snapshot.toObjects(Goal::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
    }

    override suspend fun createGoal(userId: String, goal: Goal): Result<String> {
        val collRef = db.collection("users").document(userId).collection("goals")
        val docRef = if (goal.id.isNotBlank()) collRef.document(goal.id) else collRef.document()
        val payload = mutableMapOf<String, Any>(
            "userId" to userId,
            "title" to goal.title,
            "period" to goal.period,
            "category" to goal.category,
            "targetCount" to goal.targetCount,
            "currentProgress" to goal.currentProgress,
            "isCompleted" to goal.isCompleted,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (goal.completedAt != null) {
            payload["completedAt"] = goal.completedAt
        }
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun updateGoalProgress(
        userId: String,
        goalId: String,
        newProgress: Long,
        isCompleted: Boolean
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("goals").document(goalId)
        val payload = mutableMapOf<String, Any>(
            "currentProgress" to newProgress,
            "isCompleted" to isCompleted,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCompleted) {
            payload["completedAt"] = FieldValue.serverTimestamp()
        }
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteGoal(userId: String, goalId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("goals").document(goalId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- DAILY CHECK-INS ---

    override fun observeCheckIns(userId: String): Flow<List<DailyCheckIn>> {
        val collRef = db.collection("users").document(userId).collection("check_ins")
        return collRef.snapshots().map { snapshot ->
            snapshot.toObjects(DailyCheckIn::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
    }

    override suspend fun logCheckIn(userId: String, checkIn: DailyCheckIn): Result<Unit> {
        val docId = if (checkIn.id.isNotBlank()) checkIn.id else if (checkIn.date.isNotBlank()) checkIn.date else db.collection("users").document(userId).collection("check_ins").document().id
        val docRef = db.collection("users").document(userId).collection("check_ins").document(docId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updatePayload = mapOf(
                    "status" to checkIn.status,
                    "cravingLevel" to checkIn.cravingLevel,
                    "mood" to checkIn.mood,
                    "notes" to checkIn.notes,
                    "triggers" to checkIn.triggers,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to userId,
                    "date" to checkIn.date,
                    "status" to checkIn.status,
                    "cravingLevel" to checkIn.cravingLevel,
                    "mood" to checkIn.mood,
                    "notes" to checkIn.notes,
                    "triggers" to checkIn.triggers,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- REWARDS & BADGES ---

    override fun observeRewards(userId: String): Flow<List<Reward>> {
        val collRef = db.collection("users").document(userId).collection("rewards")
        return collRef.snapshots().map { snapshot ->
            snapshot.toObjects(Reward::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
    }

    override suspend fun unlockReward(userId: String, reward: Reward): Result<String> {
        val collRef = db.collection("users").document(userId).collection("rewards")
        val docRef = if (reward.id.isNotBlank()) collRef.document(reward.id) else collRef.document()
        val payload = mapOf(
            "userId" to userId,
            "title" to reward.title,
            "description" to reward.description,
            "iconName" to reward.iconName,
            "badgeType" to reward.badgeType,
            "pointsAwarded" to reward.pointsAwarded,
            "positiveReinforcement" to reward.positiveReinforcement,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            if (reward.pointsAwarded > 0) {
                awardPoints(userId, reward.pointsAwarded)
            }
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- CUSTOM DOPAMINE REWARDS ---

    override fun observeCustomRewards(userId: String): Flow<List<CustomReward>> {
        val collRef = db.collection("users").document(userId).collection("custom_rewards")
        return collRef.snapshots().map { snapshot ->
            snapshot.toObjects(CustomReward::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
    }

    override suspend fun createCustomReward(userId: String, reward: CustomReward): Result<String> {
        val collRef = db.collection("users").document(userId).collection("custom_rewards")
        val docRef = if (reward.id.isNotBlank()) collRef.document(reward.id) else collRef.document()
        val payload = mapOf(
            "userId" to userId,
            "title" to reward.title,
            "costPoints" to reward.costPoints,
            "isRedeemed" to reward.isRedeemed,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun redeemCustomReward(userId: String, rewardId: String, costPoints: Long): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("custom_rewards").document(rewardId)
        val payload = mapOf(
            "isRedeemed" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(payload).await()
            // Deduct points
            val userRef = db.collection("users").document(userId)
            userRef.update(
                mapOf(
                    "points" to FieldValue.increment(-costPoints),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomReward(userId: String, rewardId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("custom_rewards").document(rewardId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
