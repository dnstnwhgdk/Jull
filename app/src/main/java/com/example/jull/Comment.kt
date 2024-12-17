package com.example.jull

import com.google.firebase.Timestamp
import java.util.Date


data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val content: String = "",
    val createdAt: Date = Date()
) {
    companion object {
        fun fromMap(id: String, data: Map<String, Any>): Comment {
            return Comment(
                id = id,
                postId = data["postId"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                authorName = data["authorName"] as? String ?: "",
                content = data["content"] as? String ?: "",
                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date()
            )
        }
    }

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "postId" to postId,
            "userId" to userId,
            "authorName" to authorName,
            "content" to content,
            "createdAt" to createdAt
        )
    }
}