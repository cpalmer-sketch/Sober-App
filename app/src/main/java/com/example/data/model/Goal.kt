package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Goal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val period: String = "daily", // daily, weekly, monthly
    val category: String = "mindfulness", // mindfulness, health, activity, support, custom
    val targetCount: Long = 1L,
    val currentProgress: Long = 0L,
    val isCompleted: Boolean = false,
    val completedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
