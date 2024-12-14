package com.example.jull

import com.google.firebase.Timestamp
import java.util.Date

data class Item(
    val id: String = "",          // Firestore 문서 ID
    val sellerId: String = "",    // 판매자 ID (Firebase Auth의 UID)
    val imageUrl: String = "",    // 상품 이미지 URL
    val title: String = "",       // 상품명
    val subtitle: String = "",    // 부제목
    val category: String = "",    // 카테고리
    val price: String = "",       // 가격
    val description: String = "", // 상품 설명
    val createdAt: Date = Date(), // 등록 시간
    val status: String = "판매중"  // 상품 상태 (판매중, 예약중, 판매완료)
) {
    // Firestore 문서를 Item 객체로 변환하는 companion object
    companion object {
        fun fromMap(id: String, data: Map<String, Any>): Item {
            return Item(
                id = id,
                sellerId = data["sellerId"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                title = data["title"] as? String ?: "",
                subtitle = data["subtitle"] as? String ?: "",
                category = data["category"] as? String ?: "",
                price = data["price"] as? String ?: "",
                description = data["description"] as? String ?: "",
                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                status = data["status"] as? String ?: "판매중"
            )
        }
    }

    // Item 객체를 Firestore 문서로 변환하는 함수
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "sellerId" to sellerId,
            "imageUrl" to imageUrl,
            "title" to title,
            "subtitle" to subtitle,
            "category" to category,
            "price" to price,
            "description" to description,
            "createdAt" to createdAt,
            "status" to status
        )
    }
}