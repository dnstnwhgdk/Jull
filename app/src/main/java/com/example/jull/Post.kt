package com.example.jull

import com.google.firebase.Timestamp
import java.util.Date

data class Post(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val authorName: String = "",
    val createdAt: Date = Date(),
    val commentCount: Int = 0,
    val likeCount: Int = 0
) {
    companion object {
        fun fromMap(id: String, data: Map<String, Any>): Post {
            return Post(
                id = id,
                userId = data["userId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                authorName = data["authorName"] as? String ?: "",
                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                commentCount = (data["commentCount"] as? Long)?.toInt() ?: 0,
                likeCount = (data["likeCount"] as? Long)?.toInt() ?: 0
            )
        }
    }

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "userId" to userId,
            "title" to title,
            "content" to content,
            "authorName" to authorName,
            "createdAt" to createdAt,
            "commentCount" to commentCount,
            "likeCount" to likeCount
        )
    }
}