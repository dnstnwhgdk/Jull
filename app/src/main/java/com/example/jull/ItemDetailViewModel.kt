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

        // 기존 채팅방 찾기
        chatRoomsRef
            .whereEqualTo("itemId", itemId)
            .whereEqualTo("sellerId", sellerId)
            .whereEqualTo("buyerId", buyerId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // 채팅방이 없으면 새로 생성
                    val currentTime = System.currentTimeMillis()
                    val newChatRoom = hashMapOf(
                        "itemId" to itemId,
                        "sellerId" to sellerId,
                        "buyerId" to buyerId,
                        "itemPhotoUrl" to itemPhotoUrl, // 아이템 사진 추가
                        "createdAt" to currentTime // 생성 시간 추가
                    )
                    chatRoomsRef.add(newChatRoom)
                        .addOnSuccessListener { documentReference ->
                            val chatRoomId = documentReference.id

                            val welcomeMessage = hashMapOf(
                                "senderId" to sellerId,
                                "content" to "안녕하세요! 채팅을 시작해보세요.",
                                "timestamp" to currentTime
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
