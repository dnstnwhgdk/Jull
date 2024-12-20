package com.example.jull

data class ChatRoom(
    val id: String = "",
    val itemId: String = "",
    val sellerId: String = "",
    val buyerId: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val imageUrl: String = ""
)