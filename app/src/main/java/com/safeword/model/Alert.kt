package com.safeword.model

data class Alert(
    val triggeredById: String = "",
    val triggeredByName: String = "",
    val safeWord: String = "",
    val urgency: Int = 1,
    val currentFriendId: String = "",
    val callRequested: Boolean = false,
    val imageUrls: List<String> = listOf(),
    val videoUrl: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)
