package com.example.jull

import com.google.firebase.Timestamp
import java.util.Date

data class Item(
    val id: String = "",
    val sellerId: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val subtitle: String = "",
    val brandCategory: String = "",
    val effecterType: String = "",
    val price: String = "",
    val description: String = "",
    val createdAt: Date = Date(),
    val status: String = "판매중",
    val tradeType: String = "택배거래"

) {
    companion object {
        fun fromMap(id: String, data: Map<String, Any>): Item {
            return Item(
                id = id,
                sellerId = data["sellerId"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                title = data["title"] as? String ?: "",
                subtitle = data["subtitle"] as? String ?: "",
                brandCategory = data["brandCategory"] as? String ?: "",
                effecterType = data["effecterType"] as? String ?: "",
                price = data["price"] as? String ?: "",
                description = data["description"] as? String ?: "",
                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                status = data["status"] as? String ?: "판매중",
                tradeType = data["tradeType"] as? String ?: "택배거래"
            )
        }
    }

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "sellerId" to sellerId,
            "imageUrl" to imageUrl,
            "title" to title,
            "subtitle" to subtitle,
            "brandCategory" to brandCategory,
            "effecterType" to effecterType,
            "price" to price,
            "description" to description,
            "createdAt" to createdAt,
            "status" to status,
            "tradeType" to tradeType
        )
    }
}