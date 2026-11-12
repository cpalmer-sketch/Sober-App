package com.example.ui.recovery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CustomReward
import com.example.data.model.DailyCheckIn
import com.example.data.model.Goal
import com.example.data.model.Reward
import com.example.data.model.UserProfile
import com.example.data.repository.RecoveryRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val TAG = "RecoveryVM"
private const val STOP_TIMEOUT_MILLIS = 5000L

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class CelebrationEvent(
    val title: String,
    val message: String,
    val pointsAwarded: Long,
    val badgeIcon: String = "bolt"
)

class RecoveryViewModel(
    private val repository: RecoveryRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _celebrationEvent = MutableSharedFlow<CelebrationEvent>()
    val celebrationEvent: SharedFlow<CelebrationEvent> = _celebrationEvent.asSharedFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val profile: StateFlow<UiState<UserProfile?>> = repository.observeProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapError(error)
            Log.w(TAG, "Error observing profile", root)
            emit(UiState.Error(root.message ?: "Failed to observe profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val goals: StateFlow<UiState<List<Goal>>> = repository.observeGoals(currentUserId)
        .map<List<Goal>, UiState<List<Goal>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapError(error)
            Log.w(TAG, "Error observing goals", root)
            emit(UiState.Error(root.message ?: "Failed to observe goals"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val checkIns: StateFlow<UiState<List<DailyCheckIn>>> = repository.observeCheckIns(currentUserId)
        .map<List<DailyCheckIn>, UiState<List<DailyCheckIn>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapError(error)
            Log.w(TAG, "Error observing check-ins", root)
            emit(UiState.Error(root.message ?: "Failed to observe check-ins"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val rewards: StateFlow<UiState<List<Reward>>> = repository.observeRewards(currentUserId)
        .map<List<Reward>, UiState<List<Reward>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapError(error)
            Log.w(TAG, "Error observing rewards", root)
            emit(UiState.Error(root.message ?: "Failed to observe rewards"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val customRewards: StateFlow<UiState<List<CustomReward>>> = repository.observeCustomRewards(currentUserId)
        .map<List<CustomReward>, UiState<List<CustomReward>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapError(error)
            Log.w(TAG, "Error observing custom rewards", root)
            emit(UiState.Error(root.message ?: "Failed to observe custom rewards"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    init {
        initializeUserDataIfMissing()
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    private fun initializeUserDataIfMissing() {
        viewModelScope.launch {
            try {
                val current = repository.getProfile(currentUserId)
                if (current == null) {
                    val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    repository.createProfile(currentUserId, todayIso, "Substances / Drugs")

                    // Create starter goals to boost early momentum
                    repository.createGoal(
                        currentUserId,
                        Goal(
                            userId = currentUserId,
                            title = "Morning Mindful Breathing (5 mins)",
                            period = "daily",
                            category = "mindfulness",
                            targetCount = 1L,
                            currentProgress = 0L
                        )
                    )
                    repository.createGoal(
                        currentUserId,
                        Goal(
                            userId = currentUserId,
                            title = "Drink 8 Glasses of Water",
                            period = "daily",
                            category = "health",
                            targetCount = 8L,
                            currentProgress = 0L
                        )
                    )
                    repository.createGoal(
                        currentUserId,
                        Goal(
                            userId = currentUserId,
                            title = "Attend Support Group or Call a Friend",
                            period = "weekly",
                            category = "support",
                            targetCount = 2L,
                            currentProgress = 0L
                        )
                    )
                    repository.createGoal(
                        currentUserId,
                        Goal(
                            userId = currentUserId,
                            title = "Surfe Cravings Without Giving In",
                            period = "weekly",
                            category = "activity",
                            targetCount = 3L,
                            currentProgress = 0L
                        )
                    )

                    // Starter custom rewards
                    repository.createCustomReward(
                        currentUserId,
                        CustomReward(
                            userId = currentUserId,
                            title = "Specialty Iced Coffee / Smoothie",
                            costPoints = 60L
                        )
                    )
                    repository.createCustomReward(
                        currentUserId,
                        CustomReward(
                            userId = currentUserId,
                            title = "Movie Night with Favorite Snack",
                            costPoints = 120L
                        )
                    )
                    repository.createCustomReward(
                        currentUserId,
                        CustomReward(
                            userId = currentUserId,
                            title = "New Book, Game, or Music Album",
                            costPoints = 250L
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed initial user setup", e)
            }
        }
    }

    // --- GOAL ACTIONS ---

    fun createGoal(title: String, period: String, category: String, targetCount: Long) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val goal = Goal(
                userId = currentUserId,
                title = title.trim(),
                period = period,
                category = category,
                targetCount = targetCount.coerceAtLeast(1L),
                currentProgress = 0L,
                isCompleted = false
            )
            val result = repository.createGoal(currentUserId, goal)
            if (result.isSuccess) {
                _userMessage.value = "Goal created! Stay consistent."
            } else {
                _userMessage.value = "Failed to save goal"
            }
        }
    }

    fun incrementGoalProgress(goal: Goal) {
        viewModelScope.launch {
            val newProgress = goal.currentProgress + 1L
            val isNowComplete = newProgress >= goal.targetCount
            val result = repository.updateGoalProgress(
                currentUserId,
                goal.id,
                newProgress,
                isNowComplete
            )
            if (result.isSuccess) {
                if (isNowComplete && !goal.isCompleted) {
                    val pts = when (goal.period) {
                        "daily" -> 40L
                        "weekly" -> 80L
                        "monthly" -> 160L
                        else -> 50L
                    }
                    repository.awardPoints(currentUserId, pts)
                    _celebrationEvent.emit(
                        CelebrationEvent(
                            title = "Goal Completed! 🎉",
                            message = "You finished '${goal.title}'! Your dopamine reward is credited.",
                            pointsAwarded = pts,
                            badgeIcon = "check_circle"
                        )
                    )
                    checkGoalCountMilestones()
                } else {
                    repository.awardPoints(currentUserId, 10L)
                    _userMessage.value = "+10 Dopamine Points! Progress logged."
                }
            }
        }
    }

    fun toggleGoalComplete(goal: Goal) {
        viewModelScope.launch {
            val newStatus = !goal.isCompleted
            val newProgress = if (newStatus) goal.targetCount else 0L
            val result = repository.updateGoalProgress(
                currentUserId,
                goal.id,
                newProgress,
                newStatus
            )
            if (result.isSuccess && newStatus) {
                val pts = 35L
                repository.awardPoints(currentUserId, pts)
                _celebrationEvent.emit(
                    CelebrationEvent(
                        title = "Goal Completed!",
                        message = "Awesome follow-through on '${goal.title}'!",
                        pointsAwarded = pts,
                        badgeIcon = "emoji_events"
                    )
                )
                checkGoalCountMilestones()
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoal(currentUserId, goalId)
            _userMessage.value = "Goal deleted."
        }
    }

    // --- DAILY CHECK-IN ACTIONS ---

    fun submitDailyCheckIn(
        status: String, // "sober" or "struggled"
        cravingLevel: Long,
        mood: String,
        notes: String,
        triggers: List<String>
    ) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val checkIn = DailyCheckIn(
                id = todayStr,
                userId = currentUserId,
                date = todayStr,
                status = status,
                cravingLevel = cravingLevel,
                mood = mood,
                notes = notes.trim(),
                triggers = triggers
            )
            val result = repository.logCheckIn(currentUserId, checkIn)
            if (result.isSuccess) {
                val pts = if (status == "sober") 30L else 15L
                repository.awardPoints(currentUserId, pts)

                // Update streak in profile
                val currentProfile = (profile.value as? UiState.Success)?.data
                if (currentProfile != null) {
                    val newCurrentStreak = if (status == "sober") currentProfile.currentStreakDays + 1L else 0L
                    val newLongest = maxOf(currentProfile.longestStreakDays, newCurrentStreak)
                    repository.updateProfile(
                        currentUserId,
                        currentProfile.copy(
                            currentStreakDays = newCurrentStreak,
                            longestStreakDays = newLongest
                        )
                    )
                    checkSobrietyMilestones(newCurrentStreak)
                }

                _celebrationEvent.emit(
                    CelebrationEvent(
                        title = if (status == "sober") "Day Won! Clean & Sober 🌟" else "Honesty is Strength 🤝",
                        message = if (status == "sober")
                            "Logging daily check-ins rewires your brain to seek satisfaction in healthy consistency!"
                        else
                            "Recovery is not linear. Acknowledging cravings without giving in builds lasting resilience.",
                        pointsAwarded = pts,
                        badgeIcon = if (status == "sober") "star" else "favorite"
                    )
                )
                checkCheckInMilestones()
            } else {
                _userMessage.value = "Could not record check-in. Try again."
            }
        }
    }

    // --- CUSTOM DOPAMINE REWARDS ---

    fun createCustomReward(title: String, costPoints: Long) {
        if (title.isBlank() || costPoints <= 0) return
        viewModelScope.launch {
            val reward = CustomReward(
                userId = currentUserId,
                title = title.trim(),
                costPoints = costPoints,
                isRedeemed = false
            )
            val result = repository.createCustomReward(currentUserId, reward)
            if (result.isSuccess) {
                _userMessage.value = "Reward added to your Dopamine Store!"
            }
        }
    }

    fun redeemCustomReward(reward: CustomReward) {
        val currentProfile = (profile.value as? UiState.Success)?.data ?: return
        if (currentProfile.points < reward.costPoints) {
            _userMessage.value = "You need ${reward.costPoints - currentProfile.points} more points to redeem this reward."
            return
        }
        viewModelScope.launch {
            val result = repository.redeemCustomReward(currentUserId, reward.id, reward.costPoints)
            if (result.isSuccess) {
                _celebrationEvent.emit(
                    CelebrationEvent(
                        title = "Reward Redeemed! 🎁",
                        message = "You earned this treat through real sobriety work: '${reward.title}'! Enjoy the real dopamine hit.",
                        pointsAwarded = 0L,
                        badgeIcon = "celebration"
                    )
                )
            }
        }
    }

    fun deleteCustomReward(rewardId: String) {
        viewModelScope.launch {
            repository.deleteCustomReward(currentUserId, rewardId)
            _userMessage.value = "Reward removed."
        }
    }

    // --- RECOVERY PROFILE SETUP ---

    fun updateSobrietyStartDate(newDate: String, substanceName: String) {
        viewModelScope.launch {
            val current = (profile.value as? UiState.Success)?.data
            if (current != null) {
                repository.updateProfile(
                    currentUserId,
                    current.copy(
                        soberStartDate = newDate,
                        substanceName = substanceName.ifBlank { "Substances / Drugs" }
                    )
                )
                _userMessage.value = "Sobriety counter updated!"
            }
        }
    }

    // --- MILESTONE CHECKS ---

    private fun checkSobrietyMilestones(streakDays: Long) {
        viewModelScope.launch {
            val currentRewards = (rewards.value as? UiState.Success)?.data ?: emptyList()
            val existingTitles = currentRewards.map { it.title }.toSet()

            val milestones = listOf(
                Triple(1L, "Day One Warrior", "24 hours clean! Your body begins clearing toxins and natural receptors awaken."),
                Triple(3L, "72-Hour Power", "3 full days sober! Peak withdrawal symptoms are fading; neurochemistry is rebalancing."),
                Triple(7L, "1 Week Dopamine Reset", "7 consecutive days! Your dopamine receptors are actively rebuilding sensitivity."),
                Triple(14L, "Two Weeks Strong", "14 days clean! Clearer sleep, lower anxiety, and stronger willpower pathways."),
                Triple(30L, "One Month Milestone 🏆", "30 days sober! Brain scans show major neuroplastic healing in executive function!"),
                Triple(60L, "Two Months Freedom", "60 days clean! A new identity of strength and true vitality is solidified."),
                Triple(90L, "Ninety Days Mastery", "90 days sober! Complete seasonal cycle clean. Life transformed!")
            )

            for ((reqDays, title, reinforcement) in milestones) {
                if (streakDays >= reqDays && !existingTitles.contains(title)) {
                    val pts = reqDays * 15L + 50L
                    val reward = Reward(
                        id = "milestone_" + reqDays + "d",
                        userId = currentUserId,
                        title = title,
                        description = "Achieved $reqDays days clean and sober.",
                        iconName = "emoji_events",
                        badgeType = "milestone",
                        pointsAwarded = pts,
                        positiveReinforcement = reinforcement
                    )
                    repository.unlockReward(currentUserId, reward)
                    _celebrationEvent.emit(
                        CelebrationEvent(
                            title = "New Milestone Unlocked: $title",
                            message = reinforcement,
                            pointsAwarded = pts,
                            badgeIcon = "emoji_events"
                        )
                    )
                }
            }
        }
    }

    private fun checkGoalCountMilestones() {
        viewModelScope.launch {
            val allGoals = (goals.value as? UiState.Success)?.data ?: emptyList()
            val completedCount = allGoals.count { it.isCompleted }
            val currentRewards = (rewards.value as? UiState.Success)?.data ?: emptyList()
            val existingTitles = currentRewards.map { it.title }.toSet()

            if (completedCount >= 5 && !existingTitles.contains("Goal Master (5)")) {
                val reward = Reward(
                    id = "badge_goals_5",
                    userId = currentUserId,
                    title = "Goal Master (5)",
                    description = "Completed 5 recovery goals.",
                    iconName = "military_tech",
                    badgeType = "goals",
                    pointsAwarded = 75L,
                    positiveReinforcement = "Small daily wins release sustained healthy dopamine, weakening old drug cravings!"
                )
                repository.unlockReward(currentUserId, reward)
            }
        }
    }

    private fun checkCheckInMilestones() {
        viewModelScope.launch {
            val allCheckIns = (checkIns.value as? UiState.Success)?.data ?: emptyList()
            val currentRewards = (rewards.value as? UiState.Success)?.data ?: emptyList()
            val existingTitles = currentRewards.map { it.title }.toSet()

            if (allCheckIns.size >= 7 && !existingTitles.contains("Mindful Observer (7 Logs)")) {
                val reward = Reward(
                    id = "badge_checkins_7",
                    userId = currentUserId,
                    title = "Mindful Observer (7 Logs)",
                    description = "Logged 7 daily sobriety check-ins.",
                    iconName = "insights",
                    badgeType = "check_in",
                    pointsAwarded = 80L,
                    positiveReinforcement = "Daily self-awareness creates emotional regulation, the foundation of lasting recovery."
                )
                repository.unlockReward(currentUserId, reward)
            }
        }
    }

    private fun unwrapError(error: Throwable): Throwable {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        return firestoreError ?: error
    }
}
