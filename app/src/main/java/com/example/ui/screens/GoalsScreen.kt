package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Goal
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold500
import com.example.ui.theme.Indigo500

@Composable
fun GoalsScreen(
    goals: List<Goal>,
    onCreateGoal: (String, String, String, Long) -> Unit,
    onIncrementGoal: (Goal) -> Unit,
    onToggleGoalComplete: (Goal) -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    var selectedPeriodTab by remember { mutableIntStateOf(0) }
    val periodTabs = listOf("All", "Daily", "Weekly", "Monthly")
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredGoals = remember(goals, selectedPeriodTab) {
        when (selectedPeriodTab) {
            1 -> goals.filter { it.period.equals("daily", ignoreCase = true) }
            2 -> goals.filter { it.period.equals("weekly", ignoreCase = true) }
            3 -> goals.filter { it.period.equals("monthly", ignoreCase = true) }
            else -> goals
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Period Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedPeriodTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Gold500,
                modifier = Modifier.fillMaxWidth().testTag("goals_period_tabs")
            ) {
                periodTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedPeriodTab == index,
                        onClick = { selectedPeriodTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedPeriodTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            if (filteredGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Gold500.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No goals in this category yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Set small daily and weekly targets to trigger healthy dopamine hits without substances.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_add_goal_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF261800))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create a Goal", color = Color(0xFF261800), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredGoals, key = { it.id }) { goal ->
                        GoalItemCard(
                            goal = goal,
                            onIncrement = { onIncrementGoal(goal) },
                            onToggleComplete = { onToggleGoalComplete(goal) },
                            onDelete = { onDeleteGoal(goal.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Goal
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Gold500,
            contentColor = Color(0xFF261800),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_goal")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Goal", modifier = Modifier.size(28.dp))
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            defaultPeriod = if (selectedPeriodTab in 1..3) periodTabs[selectedPeriodTab].lowercase() else "daily",
            onDismiss = { showAddDialog = false },
            onCreate = { title, period, category, target ->
                onCreateGoal(title, period, category, target)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun GoalItemCard(
    goal: Goal,
    onIncrement: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val progressFraction = (goal.currentProgress.toFloat() / goal.targetCount.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "goal_progress")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.isCompleted)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (goal.isCompleted) 1.dp else 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_card_${goal.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(36.dp).testTag("goal_toggle_${goal.id}")
                    ) {
                        Icon(
                            imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (goal.isCompleted) "Completed" else "Incomplete",
                            tint = if (goal.isCompleted) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (goal.isCompleted)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Gold500.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = goal.period.uppercase(),
                                    color = Gold500,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "•  ${goal.category.replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Goal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progress: ${goal.currentProgress} / ${goal.targetCount}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (goal.isCompleted) Emerald500 else Gold500
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.isCompleted) Emerald500 else Gold500,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (!goal.isCompleted) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onIncrement,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("goal_increment_${goal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Gold500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+1 Done (+10 Sparks)",
                            color = Gold500,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGoalDialog(
    defaultPeriod: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(defaultPeriod) }
    var selectedCategory by remember { mutableStateOf("mindfulness") }
    var targetCount by remember { mutableLongStateOf(1L) }

    val quickSuggestions = listOf(
        Pair("Morning breathwork & meditation", "mindfulness"),
        Pair("30 min physical exercise / workout", "health"),
        Pair("Call sponsor or trusted friend", "support"),
        Pair("Go for a fresh air nature walk", "activity"),
        Pair("Drink 8 glasses of water", "health"),
        Pair("Read 10 pages of recovery book", "mindfulness"),
        Pair("Attend recovery / 12-step meeting", "support"),
        Pair("Write 3 things I'm grateful for", "mindfulness")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("add_goal_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Create Recovery Goal",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. 20 min evening walk") },
                    modifier = Modifier.fillMaxWidth().testTag("add_goal_title_input"),
                    singleLine = true
                )

                // Quick suggestions chips
                Text(
                    text = "Quick Recovery Suggestions:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickSuggestions) { (suggestedTitle, cat) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    title = suggestedTitle
                                    selectedCategory = cat
                                }
                        ) {
                            Text(
                                text = suggestedTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Timeframe / Period Selector
                Text(
                    text = "Goal Timeframe:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("daily", "weekly", "monthly").forEach { p ->
                        FilterChip(
                            selected = selectedPeriod == p,
                            onClick = { selectedPeriod = p },
                            label = { Text(p.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold500,
                                selectedLabelColor = Color(0xFF261800)
                            )
                        )
                    }
                }

                // Category Selector
                Text(
                    text = "Category:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("mindfulness", "health", "activity", "support", "custom")) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Indigo500,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Target Repetitions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target Repetitions: $targetCount",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (targetCount > 1) targetCount-- },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-", style = MaterialTheme.typography.titleLarge, color = Gold500)
                        }
                        Text("$targetCount", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                        IconButton(
                            onClick = { if (targetCount < 30) targetCount++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge, color = Gold500)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title.trim(), selectedPeriod, selectedCategory, targetCount)
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        modifier = Modifier.testTag("save_new_goal_button")
                    ) {
                        Text("Save Goal", color = Color(0xFF261800), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
