package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CustomReward
import com.example.data.model.Reward
import com.example.data.model.UserProfile
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold500
import com.example.ui.theme.Indigo500

data class StandardMilestone(
    val id: String,
    val title: String,
    val requirement: String,
    val pointsAwarded: Long,
    val positiveReinforcement: String
)

@Composable
fun RewardsScreen(
    profile: UserProfile?,
    rewards: List<Reward>,
    customRewards: List<CustomReward>,
    onRedeemCustomReward: (CustomReward) -> Unit,
    onCreateCustomReward: (String, Long) -> Unit,
    onDeleteCustomReward: (String) -> Unit
) {
    var selectedBadgeForDetail by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAddRewardDialog by remember { mutableStateOf(false) }

    val unlockedTitleMap = remember(rewards) {
        rewards.associateBy { it.title }
    }

    val standardMilestones = remember {
        listOf(
            StandardMilestone(
                id = "milestone_1d",
                title = "Day One Warrior",
                requirement = "1 Day Clean",
                pointsAwarded = 65L,
                positiveReinforcement = "24 hours clean! Your body is flushing chemical toxins. Your brain's native endorphins and serotonin systems are reawakening."
            ),
            StandardMilestone(
                id = "milestone_3d",
                title = "72-Hour Power",
                requirement = "3 Days Clean",
                pointsAwarded = 95L,
                positiveReinforcement = "3 full days! The hardest acute withdrawal peak is behind you. Cellular recovery in the prefrontal cortex is taking hold."
            ),
            StandardMilestone(
                id = "milestone_7d",
                title = "1 Week Dopamine Reset",
                requirement = "7 Days Clean",
                pointsAwarded = 155L,
                positiveReinforcement = "7 consecutive days! Downregulated dopamine receptors are actively proliferating, increasing your capacity for natural joy."
            ),
            StandardMilestone(
                id = "milestone_14d",
                title = "Two Weeks Strong",
                requirement = "14 Days Clean",
                pointsAwarded = 260L,
                positiveReinforcement = "Two weeks clean! Sleep architecture has normalized, emotional regulation pathways have strengthened, and cravings are weaker."
            ),
            StandardMilestone(
                id = "milestone_30d",
                title = "One Month Milestone 🏆",
                requirement = "30 Days Clean",
                pointsAwarded = 500L,
                positiveReinforcement = "30 full days! A profound clinical milestone. Neuroimaging shows substantial regeneration of gray matter in executive control centers!"
            ),
            StandardMilestone(
                id = "milestone_60d",
                title = "Two Months Freedom",
                requirement = "60 Days Clean",
                pointsAwarded = 750L,
                positiveReinforcement = "60 days sober! You have built a foundation of genuine independence. Your life is guided by choice rather than compulsion."
            ),
            StandardMilestone(
                id = "milestone_90d",
                title = "Ninety Days Mastery",
                requirement = "90 Days Clean",
                pointsAwarded = 1200L,
                positiveReinforcement = "90 days clean! A full season of sobriety. Your dopamine baseline is restored and natural healthy pleasures bring sustained satisfaction."
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero: Dopamine Sparks Balance
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("rewards_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Gold300.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Gold500.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${profile?.points ?: 0} Sparks",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                            color = Gold500
                        )

                        Text(
                            text = "Dopamine Rewards Bank",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Earned through real milestones and sobriety goals. Redeem for real-life healthy treats below!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Section: Sobriety Milestone Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Gold500,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sobriety Milestone Badges",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        items(standardMilestones) { milestone ->
            val unlockedReward = unlockedTitleMap[milestone.title]
            val isUnlocked = unlockedReward != null
            val reinforcementText = unlockedReward?.positiveReinforcement ?: milestone.positiveReinforcement

            MilestoneBadgeCard(
                milestone = milestone,
                isUnlocked = isUnlocked,
                onClick = {
                    selectedBadgeForDetail = Pair(milestone.title, reinforcementText)
                }
            )
        }

        // Section: Dopamine Store (Healthy Real-Life Treats)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Redeem,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dopamine Store",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = { showAddRewardDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_custom_reward_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Treat", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (customRewards.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Add healthy rewards you look forward to (e.g., specialty coffee, new game, movie night, favorite meal).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(customRewards, key = { it.id }) { reward ->
                CustomRewardCard(
                    reward = reward,
                    userPoints = profile?.points ?: 0L,
                    onRedeem = { onRedeemCustomReward(reward) },
                    onDelete = { onDeleteCustomReward(reward.id) }
                )
            }
        }
    }

    // Detail dialog for badge positive reinforcement
    if (selectedBadgeForDetail != null) {
        val (title, message) = selectedBadgeForDetail!!
        Dialog(onDismissRequest = { selectedBadgeForDetail = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Gold500.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Gold500,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { selectedBadgeForDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome!", color = Color(0xFF261800), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddRewardDialog) {
        AddCustomRewardDialog(
            onDismiss = { showAddRewardDialog = false },
            onCreate = { title, cost ->
                onCreateCustomReward(title, cost)
                showAddRewardDialog = false
            }
        )
    }
}

@Composable
private fun MilestoneBadgeCard(
    milestone: StandardMilestone,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("milestone_${milestone.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (isUnlocked) Gold500.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isUnlocked) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = milestone.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${milestone.requirement} • +${milestone.pointsAwarded} Sparks",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isUnlocked) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isUnlocked) Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (isUnlocked) "UNLOCKED ✨" else "LOCKED",
                    color = if (isUnlocked) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomRewardCard(
    reward: CustomReward,
    userPoints: Long,
    onRedeem: () -> Unit,
    onDelete: () -> Unit
) {
    val canAfford = userPoints >= reward.costPoints

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("custom_reward_${reward.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Gold500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${reward.costPoints} Sparks",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Gold500
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onRedeem,
                    enabled = canAfford && !reward.isRedeemed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (reward.isRedeemed) Emerald500 else Gold500
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("redeem_button_${reward.id}")
                ) {
                    Text(
                        text = if (reward.isRedeemed) "Redeemed ✓" else "Redeem",
                        color = if (reward.isRedeemed) Color.White else Color(0xFF261800),
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCustomRewardDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var costPoints by remember { mutableLongStateOf(60L) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Dopamine Treat",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Treat Title") },
                    placeholder = { Text("e.g. Specialty coffee, Movie night, New book") },
                    modifier = Modifier.fillMaxWidth().testTag("custom_reward_title_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cost: $costPoints Sparks",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Gold500
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (costPoints > 20) costPoints -= 20 }) {
                            Text("-20", fontWeight = FontWeight.Bold, color = Gold500)
                        }
                        IconButton(onClick = { if (costPoints < 1000) costPoints += 20 }) {
                            Text("+20", fontWeight = FontWeight.Bold, color = Gold500)
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
                                onCreate(title.trim(), costPoints)
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        modifier = Modifier.testTag("save_custom_reward_button")
                    ) {
                        Text("Add Treat", color = Color(0xFF261800), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
