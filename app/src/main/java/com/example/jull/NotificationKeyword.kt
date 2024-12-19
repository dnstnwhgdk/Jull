package com.example.jull

import java.util.Date

data class NotificationKeyword(
    val id: String = "",
    val userId: String = "",
    val keyword: String = "",
    val createdAt: Date = Date()
)