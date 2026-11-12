package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class DailyCheckIn(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val status: String = "sober", // sober, struggled
    val cravingLevel: Long = 1L, // 1 to 5
    val mood: String = "great", // great, good, neutral, tough, hard
    val notes: String = "",
    val triggers: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
