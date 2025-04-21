package com.safeword.model

data class Contact(
    val friendId: String = "",
    val friendName: String = "",
    val friendEmail: String = "",
    val addedAt: Long? = null
)
