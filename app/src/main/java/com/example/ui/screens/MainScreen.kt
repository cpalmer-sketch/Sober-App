package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CelebrationOverlay
import com.example.ui.recovery.CelebrationEvent
import com.example.ui.recovery.RecoveryViewModel
import com.example.ui.recovery.UiState
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold500
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: RecoveryViewModel,
    onSignOut: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeCelebration by remember { mutableStateOf<CelebrationEvent?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val profileState by viewModel.profile.collectAsStateWithLifecycle()
    val goalsState by viewModel.goals.collectAsStateWithLifecycle()
    val checkInsState by viewModel.checkIns.collectAsStateWithLifecycle()
    val rewardsState by viewModel.rewards.collectAsStateWithLifecycle()
    val customRewardsState by viewModel.customRewards.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.celebrationEvent.collectLatest { event ->
            activeCelebration = event
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val currentProfile = (profileState as? UiState.Success)?.data
    val goalsList = (goalsState as? UiState.Success)?.data ?: emptyList()
    val checkInsList = (checkInsState as? UiState.Success)?.data ?: emptyList()
    val rewardsList = (rewardsState as? UiState.Success)?.data ?: emptyList()
    val customRewardsList = (customRewardsState as? UiState.Success)?.data ?: emptyList()

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Gold500.copy(alpha = 0.2f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SoberRewards",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (currentProfile != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Gold500.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentProfile.points} Sparks",
                                    color = Gold500,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Restart Journey") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showResetDialog = true
                                },
                                modifier = Modifier.testTag("menu_restart_journey")
                            )
                            if (onSignOut != null) {
                                DropdownMenuItem(
                                    text = { Text("Sign Out") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onSignOut()
                                    },
                                    modifier = Modifier.testTag("menu_sign_out")
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Gold500,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = "Pulse") },
                    label = { Text("Pulse") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF261800),
                        indicatorColor = Gold500,
                        selectedTextColor = Gold500
                    ),
                    modifier = Modifier.testTag("nav_tab_pulse")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Goals") },
                    label = { Text("Goals") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF261800),
                        indicatorColor = Gold500,
                        selectedTextColor = Gold500
                    ),
                    modifier = Modifier.testTag("nav_tab_goals")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Check-In") },
                    label = { Text("Check-In") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF261800),
                        indicatorColor = Gold500,
                        selectedTextColor = Gold500
                    ),
                    modifier = Modifier.testTag("nav_tab_checkin")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Rewards") },
                    label = { Text("Rewards") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF261800),
                        indicatorColor = Gold500,
                        selectedTextColor = Gold500
                    ),
                    modifier = Modifier.testTag("nav_tab_rewards")
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    profile = currentProfile,
                    goals = goalsList,
                    checkIns = checkInsList,
                    onNavigateToGoals = { selectedTab = 1 },
                    onNavigateToCheckIn = { selectedTab = 2 },
                    onNavigateToRewards = { selectedTab = 3 },
                    onUpdateStartDate = { date, substance ->
                        viewModel.updateSobrietyStartDate(date, substance)
                    },
                    onAwardUrgePoints = {
                        viewModel.submitDailyCheckIn("sober", 1L, "great", "Surfed craving with 90s breathwork!", listOf("Urge Surfed"))
                    }
                )

                1 -> GoalsScreen(
                    goals = goalsList,
                    onCreateGoal = { title, period, category, target ->
                        viewModel.createGoal(title, period, category, target)
                    },
                    onIncrementGoal = { goal ->
                        viewModel.incrementGoalProgress(goal)
                    },
                    onToggleGoalComplete = { goal ->
                        viewModel.toggleGoalComplete(goal)
                    },
                    onDeleteGoal = { goalId ->
                        viewModel.deleteGoal(goalId)
                    }
                )

                2 -> CheckInScreen(
                    checkIns = checkInsList,
                    onSubmitCheckIn = { status, craving, mood, notes, triggers ->
                        viewModel.submitDailyCheckIn(status, craving, mood, notes, triggers)
                    }
                )

                3 -> RewardsScreen(
                    profile = currentProfile,
                    rewards = rewardsList,
                    customRewards = customRewardsList,
                    onRedeemCustomReward = { reward ->
                        viewModel.redeemCustomReward(reward)
                    },
                    onCreateCustomReward = { title, cost ->
                        viewModel.createCustomReward(title, cost)
                    },
                    onDeleteCustomReward = { rewardId ->
                        viewModel.deleteCustomReward(rewardId)
                    }
                )
            }
        }
    }

    // Dopamine celebration overlay on goals and milestone events
    if (activeCelebration != null) {
        CelebrationOverlay(
            event = activeCelebration!!,
            onDismiss = { activeCelebration = null }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restart Recovery Journey?") },
            text = {
                Text("This resets your sobriety counter start date to today so you can restart your streak. Your goals and check-ins will remain saved.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                        viewModel.updateSobrietyStartDate(today, currentProfile?.substanceName ?: "Substances / Drugs")
                    }
                ) {
                    Text("Restart Today", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
