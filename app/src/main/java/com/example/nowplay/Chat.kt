package com.example.nowplay

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Chat(
    val friendId: String,
    val friendName: String,
    val chatId: String? = null
)
