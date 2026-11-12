package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.data.model.CustomReward
import com.example.data.model.DailyCheckIn
import com.example.data.model.Goal
import com.example.data.model.Reward
import com.example.data.model.UserProfile
import com.google.firebase.Timestamp
import java.util.Date

class LocalRecoveryDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "sober_rewards_local.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: LocalRecoveryDatabase? = null

        fun getInstance(context: Context): LocalRecoveryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalRecoveryDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_profiles (
                user_id TEXT PRIMARY KEY,
                sober_start_date TEXT NOT NULL,
                substance_name TEXT NOT NULL,
                points INTEGER NOT NULL DEFAULT 0,
                current_streak_days INTEGER NOT NULL DEFAULT 0,
                longest_streak_days INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER,
                updated_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goals (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                title TEXT NOT NULL,
                period TEXT NOT NULL,
                category TEXT NOT NULL,
                target_count INTEGER NOT NULL,
                current_progress INTEGER NOT NULL,
                is_completed INTEGER NOT NULL DEFAULT 0,
                completed_at INTEGER,
                created_at INTEGER,
                updated_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS daily_check_ins (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                date TEXT NOT NULL,
                status TEXT NOT NULL,
                craving_level INTEGER NOT NULL,
                mood TEXT NOT NULL,
                notes TEXT,
                triggers TEXT,
                created_at INTEGER,
                updated_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS rewards (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                icon_name TEXT NOT NULL,
                badge_type TEXT NOT NULL,
                points_awarded INTEGER NOT NULL,
                positive_reinforcement TEXT NOT NULL,
                created_at INTEGER,
                updated_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS custom_rewards (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                title TEXT NOT NULL,
                cost_points INTEGER NOT NULL,
                is_redeemed INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER,
                updated_at INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // No upgrade needed for version 1
    }

    // --- User Profile CRUD ---

    fun getUserProfile(userId: String): UserProfile? {
        val db = readableDatabase
        val cursor = db.query(
            "user_profiles",
            null,
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val createdAtMillis = it.getLongOrNull("created_at")
                val updatedAtMillis = it.getLongOrNull("updated_at")
                return UserProfile(
                    userId = it.getString(it.getColumnIndexOrThrow("user_id")),
                    soberStartDate = it.getString(it.getColumnIndexOrThrow("sober_start_date")),
                    substanceName = it.getString(it.getColumnIndexOrThrow("substance_name")),
                    points = it.getLong(it.getColumnIndexOrThrow("points")),
                    currentStreakDays = it.getLong(it.getColumnIndexOrThrow("current_streak_days")),
                    longestStreakDays = it.getLong(it.getColumnIndexOrThrow("longest_streak_days")),
                    createdAt = createdAtMillis?.let { ms -> Timestamp(Date(ms)) },
                    updatedAt = updatedAtMillis?.let { ms -> Timestamp(Date(ms)) }
                )
            }
        }
        return null
    }

    fun upsertUserProfile(profile: UserProfile) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", profile.userId)
            put("sober_start_date", profile.soberStartDate)
            put("substance_name", profile.substanceName)
            put("points", profile.points)
            put("current_streak_days", profile.currentStreakDays)
            put("longest_streak_days", profile.longestStreakDays)
            profile.createdAt?.let { put("created_at", it.toDate().time) } ?: put("created_at", System.currentTimeMillis())
            put("updated_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("user_profiles", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun addPoints(userId: String, pointsToAdd: Long) {
        val db = writableDatabase
        db.execSQL(
            "UPDATE user_profiles SET points = points + ?, updated_at = ? WHERE user_id = ?",
            arrayOf(pointsToAdd, System.currentTimeMillis(), userId)
        )
    }

    // --- Goals CRUD ---

    fun getGoals(userId: String): List<Goal> {
        val db = readableDatabase
        val list = mutableListOf<Goal>()
        val cursor = db.query(
            "goals",
            null,
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "created_at ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val completedAtMillis = it.getLongOrNull("completed_at")
                val createdAtMillis = it.getLongOrNull("created_at")
                val updatedAtMillis = it.getLongOrNull("updated_at")
                list.add(
                    Goal(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        userId = it.getString(it.getColumnIndexOrThrow("user_id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        period = it.getString(it.getColumnIndexOrThrow("period")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        targetCount = it.getLong(it.getColumnIndexOrThrow("target_count")),
                        currentProgress = it.getLong(it.getColumnIndexOrThrow("current_progress")),
                        isCompleted = it.getInt(it.getColumnIndexOrThrow("is_completed")) == 1,
                        completedAt = completedAtMillis?.let { ms -> Timestamp(Date(ms)) },
                        createdAt = createdAtMillis?.let { ms -> Timestamp(Date(ms)) },
                        updatedAt = updatedAtMillis?.let { ms -> Timestamp(Date(ms)) }
                    )
                )
            }
        }
        return list
    }

    fun upsertGoal(goal: Goal) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", goal.id)
            put("user_id", goal.userId)
            put("title", goal.title)
            put("period", goal.period)
            put("category", goal.category)
            put("target_count", goal.targetCount)
            put("current_progress", goal.currentProgress)
            put("is_completed", if (goal.isCompleted) 1 else 0)
            goal.completedAt?.let { put("completed_at", it.toDate().time) }
            goal.createdAt?.let { put("created_at", it.toDate().time) } ?: put("created_at", System.currentTimeMillis())
            put("updated_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("goals", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateGoalProgress(goalId: String, newProgress: Long, isCompleted: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("current_progress", newProgress)
            put("is_completed", if (isCompleted) 1 else 0)
            if (isCompleted) {
                put("completed_at", System.currentTimeMillis())
            }
            put("updated_at", System.currentTimeMillis())
        }
        db.update("goals", values, "id = ?", arrayOf(goalId))
    }

    fun deleteGoal(goalId: String) {
        val db = writableDatabase
        db.delete("goals", "id = ?", arrayOf(goalId))
    }

    // --- Daily Check-Ins CRUD ---

    fun getCheckIns(userId: String): List<DailyCheckIn> {
        val db = readableDatabase
        val list = mutableListOf<DailyCheckIn>()
        val cursor = db.query(
            "daily_check_ins",
            null,
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "date DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val triggersStr = it.getString(it.getColumnIndexOrThrow("triggers"))
                val triggersList = if (triggersStr.isNullOrBlank()) {
                    emptyList()
                } else {
                    triggersStr.split(",")
                }
                val createdAtMillis = it.getLongOrNull("created_at")
                val updatedAtMillis = it.getLongOrNull("updated_at")
                list.add(
                    DailyCheckIn(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        userId = it.getString(it.getColumnIndexOrThrow("user_id")),
                        date = it.getString(it.getColumnIndexOrThrow("date")),
                        status = it.getString(it.getColumnIndexOrThrow("status")),
                        cravingLevel = it.getLong(it.getColumnIndexOrThrow("craving_level")),
                        mood = it.getString(it.getColumnIndexOrThrow("mood")),
                        notes = it.getString(it.getColumnIndexOrThrow("notes")) ?: "",
                        triggers = triggersList,
                        createdAt = createdAtMillis?.let { ms -> Timestamp(Date(ms)) },
                        updatedAt = updatedAtMillis?.let { ms -> Timestamp(Date(ms)) }
                    )
                )
            }
        }
        return list
    }

    fun upsertCheckIn(checkIn: DailyCheckIn) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", checkIn.id)
            put("user_id", checkIn.userId)
            put("date", checkIn.date)
            put("status", checkIn.status)
            put("craving_level", checkIn.cravingLevel)
            put("mood", checkIn.mood)
            put("notes", checkIn.notes)
            put("triggers", checkIn.triggers.joinToString(","))
            checkIn.createdAt?.let { put("created_at", it.toDate().time) } ?: put("created_at", System.currentTimeMillis())
            put("updated_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("daily_check_ins", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // --- Rewards CRUD ---

    fun getRewards(userId: String): List<Reward> {
        val db = readableDatabase
        val list = mutableListOf<Reward>()
        val cursor = db.query(
            "rewards",
            null,
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "created_at ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val createdAtMillis = it.getLongOrNull("created_at")
                val updatedAtMillis = it.getLongOrNull("updated_at")
                list.add(
                    Reward(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        userId = it.getString(it.getColumnIndexOrThrow("user_id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        description = it.getString(it.getColumnIndexOrThrow("description")),
                        iconName = it.getString(it.getColumnIndexOrThrow("icon_name")),
                        badgeType = it.getString(it.getColumnIndexOrThrow("badge_type")),
                        pointsAwarded = it.getLong(it.getColumnIndexOrThrow("points_awarded")),
                        positiveReinforcement = it.getString(it.getColumnIndexOrThrow("positive_reinforcement")),
                        createdAt = createdAtMillis?.let { ms -> Timestamp(Date(ms)) },
                        updatedAt = updatedAtMillis?.let { ms -> Timestamp(Date(ms)) }
                    )
                )
            }
        }
        return list
    }

    fun upsertReward(reward: Reward) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", reward.id)
            put("user_id", reward.userId)
            put("title", reward.title)
            put("description", reward.description)
            put("icon_name", reward.iconName)
            put("badge_type", reward.badgeType)
            put("points_awarded", reward.pointsAwarded)
            put("positive_reinforcement", reward.positiveReinforcement)
            reward.createdAt?.let { put("created_at", it.toDate().time) } ?: put("created_at", System.currentTimeMillis())
            put("updated_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("rewards", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // --- Custom Rewards CRUD ---

    fun getCustomRewards(userId: String): List<CustomReward> {
        val db = readableDatabase
        val list = mutableListOf<CustomReward>()
        val cursor = db.query(
            "custom_rewards",
            null,
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "created_at ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val createdAtMillis = it.getLongOrNull("created_at")
                val updatedAtMillis = it.getLongOrNull("updated_at")
                list.add(
                    CustomReward(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        userId = it.getString(it.getColumnIndexOrThrow("user_id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        costPoints = it.getLong(it.getColumnIndexOrThrow("cost_points")),
                        isRedeemed = it.getInt(it.getColumnIndexOrThrow("is_redeemed")) == 1,
                        createdAt = createdAtMillis?.let { ms -> Timestamp(Date(ms)) },
                        updatedAt = updatedAtMillis?.let { ms -> Timestamp(Date(ms)) }
                    )
                )
            }
        }
        return list
    }

    fun upsertCustomReward(reward: CustomReward) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", reward.id)
            put("user_id", reward.userId)
            put("title", reward.title)
            put("cost_points", reward.costPoints)
            put("is_redeemed", if (reward.isRedeemed) 1 else 0)
            reward.createdAt?.let { put("created_at", it.toDate().time) } ?: put("created_at", System.currentTimeMillis())
            put("updated_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("custom_rewards", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun redeemCustomReward(rewardId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_redeemed", 1)
            put("updated_at", System.currentTimeMillis())
        }
        db.update("custom_rewards", values, "id = ?", arrayOf(rewardId))
    }

    fun deleteCustomReward(rewardId: String) {
        val db = writableDatabase
        db.delete("custom_rewards", "id = ?", arrayOf(rewardId))
    }

    private fun Cursor.getLongOrNull(columnName: String): Long? {
        val index = getColumnIndex(columnName)
        if (index < 0 || isNull(index)) return null
        return getLong(index)
    }
}
