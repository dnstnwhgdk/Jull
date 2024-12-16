package com.example.jull

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ItemDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    fun findOrCreateChatRoom(
        itemId: String,
        sellerId: String,
        buyerId: String,
        onChatRoomFound: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val chatRoomsRef = firestore.collection("chatRooms")

        // 기존 채팅방 찾기
        chatRoomsRef
            .whereEqualTo("itemId", itemId)
            .whereEqualTo("sellerId", sellerId)
            .whereEqualTo("buyerId", buyerId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // 채팅방이 없으면 새로 생성
                    val newChatRoom = hashMapOf(
                        "itemId" to itemId,
                        "sellerId" to sellerId,
                        "buyerId" to buyerId,
                        "messages" to emptyList<Map<String, Any>>()
                    )
                    chatRoomsRef.add(newChatRoom)
                        .addOnSuccessListener { documentReference ->
                            onChatRoomFound(documentReference.id) // 새 채팅방 ID 반환
                        }
                        .addOnFailureListener { exception ->
                            onError(exception)
                        }
                } else {
                    // 기존 채팅방으로 이동
                    val chatRoomId = querySnapshot.documents[0].id
                    onChatRoomFound(chatRoomId)
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}
