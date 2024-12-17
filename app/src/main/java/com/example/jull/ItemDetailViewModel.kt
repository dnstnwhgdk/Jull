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
                        "buyerId" to buyerId
                    )
                    chatRoomsRef.add(newChatRoom)
                        .addOnSuccessListener { documentReference ->
                            val chatRoomId = documentReference.id

                            // 새로 생성된 채팅방의 messages 서브 컬렉션에 첫 메시지 추가
                            val welcomeMessage = hashMapOf(
                                "senderId" to sellerId,
                                "content" to "안녕하세요! 채팅을 시작해보세요.",
                                "timestamp" to System.currentTimeMillis()
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
                    // 기존 채팅방 ID 반환
                    val chatRoomId = querySnapshot.documents[0].id
                    onChatRoomFound(chatRoomId)
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}
