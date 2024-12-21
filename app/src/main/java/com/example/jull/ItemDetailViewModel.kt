import androidx.lifecycle.ViewModel
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
        val chatRoomsRef = firestore.collection("chatRooms")

        chatRoomsRef
            .whereEqualTo("itemId", itemId)
            .whereEqualTo("sellerId", sellerId)
            .whereEqualTo("buyerId", buyerId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    val currentTime = System.currentTimeMillis()
                    val newChatRoom = mapOf(
                        "itemId" to itemId,
                        "sellerId" to sellerId,
                        "buyerId" to buyerId,
                        "itemPhotoUrl" to itemPhotoUrl,
                        "createdAt" to currentTime
                    )
                    chatRoomsRef.add(newChatRoom)
                        .addOnSuccessListener { documentReference ->
                            val chatRoomId = documentReference.id
                            val welcomeMessage = mapOf(
                                "senderId" to sellerId,
                                "content" to "안녕하세요! 채팅을 시작해보세요.",
                                "timestamp" to currentTime,
                                "readBy" to listOf<String>() // 읽음 상태 초기화
                            )
                            firestore.collection("chatRooms")
                                .document(chatRoomId)
                                .collection("messages")
                                .add(welcomeMessage)
                                .addOnSuccessListener {
                                    onChatRoomFound(chatRoomId)
                                }
                                .addOnFailureListener { exception ->
                                    onError(exception)
                                }
                        }
                        .addOnFailureListener { exception ->
                            onError(exception)
                        }
                } else {
                    val chatRoomId = querySnapshot.documents[0].id
                    onChatRoomFound(chatRoomId)
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}
