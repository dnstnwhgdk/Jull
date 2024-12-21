import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ItemDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    fun findOrCreateChatRoom(
        itemId: String,
        sellerId: String,
        buyerId: String,
        itemPhotoUrl: String,
        onChatRoomFound: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val chatRoomId = "$itemId-$sellerId-$buyerId"
        val chatRoomRef = firestore.collection("chatRooms").document(chatRoomId)

        chatRoomRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // 기존 채팅방이 존재
                    onChatRoomFound(chatRoomId)
                } else {
                    // 새 채팅방 생성
                    val newChatRoom = mapOf(
                        "itemId" to itemId,
                        "sellerId" to sellerId,
                        "buyerId" to buyerId,
                        "itemPhotoUrl" to itemPhotoUrl,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    chatRoomRef.set(newChatRoom)
                        .addOnSuccessListener {
                            // 웰컴 메시지 추가
                            val welcomeMessage = mapOf(
                                "senderId" to sellerId,
                                "content" to "안녕하세요! 채팅을 시작해보세요.",
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            chatRoomRef.collection("messages")
                                .add(welcomeMessage)
                                .addOnSuccessListener {
                                    onChatRoomFound(chatRoomId)
                                }
                                .addOnFailureListener { exception ->
                                    println("Error adding welcome message: ${exception.localizedMessage}")
                                    onError(exception)
                                }
                        }
                        .addOnFailureListener { exception ->
                            println("Error creating chat room: ${exception.localizedMessage}")
                            onError(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                println("Error finding chat room: ${exception.localizedMessage}")
                onError(exception)
            }
    }
}
