import androidx.lifecycle.ViewModel
import com.example.jull.ChatRoom
import com.example.jull.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatRoomsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val realtimeDatabase = FirebaseDatabase.getInstance().reference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _lastMessages = MutableStateFlow<Map<String, Triple<Message, String, String>>>(emptyMap())
    val lastMessages: StateFlow<Map<String, Triple<Message, String, String>>> = _lastMessages

    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        currentUserId?.let { userId ->
            firestore.collection("chatRooms")
                .whereIn("buyerId", listOf(userId))
                .addSnapshotListener { snapshot, _ ->
                    val buyerRooms = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(ChatRoom::class.java)?.copy(
                            id = doc.id,
                            imageUrl = doc.getString("itemPhotoUrl") ?: ""
                        )
                    } ?: emptyList()

                    firestore.collection("chatRooms")
                        .whereEqualTo("sellerId", userId)
                        .addSnapshotListener { sellerSnapshot, _ ->
                            val sellerRooms = sellerSnapshot?.documents?.mapNotNull { doc ->
                                doc.toObject(ChatRoom::class.java)?.copy(
                                    id = doc.id,
                                    imageUrl = doc.getString("itemPhotoUrl") ?: ""
                                )
                            } ?: emptyList()

                            val combinedRooms = (buyerRooms + sellerRooms).distinctBy { it.id }
                            _chatRooms.value = combinedRooms

                            // Load last messages and seller nicknames
                            loadLastMessagesAndNicknames(combinedRooms.map { it.id })
                        }
                }
        }
    }

    private fun loadLastMessagesAndNicknames(chatRoomIds: List<String>) {
        val messagesMap = mutableMapOf<String, Triple<Message, String, String>>()

        chatRoomIds.forEach { chatRoomId ->
            firestore.collection("chatRooms")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, _ ->
                    val lastMessage = snapshot?.documents?.firstOrNull()?.toObject(Message::class.java)
                    if (lastMessage != null) {
                        firestore.collection("chatRooms")
                            .document(chatRoomId)
                            .get()
                            .addOnSuccessListener { chatRoomDoc ->
                                val sellerId = chatRoomDoc.getString("sellerId") ?: "Unknown"

                                // Realtime Database에서 판매자 닉네임 가져오기
                                realtimeDatabase.child("users").child(sellerId).child("nickname")
                                    .get()
                                    .addOnSuccessListener { dataSnapshot ->
                                        val sellerNickname = dataSnapshot.getValue(String::class.java) ?: "알 수 없음"
                                        messagesMap[chatRoomId] = Triple(lastMessage, sellerNickname, sellerId)
                                        _lastMessages.value = messagesMap.toMap()
                                    }
                            }
                    }
                }
        }
    }
    fun deleteChatRoom(chatRoomId: String) {
        firestore.collection("chatRooms").document(chatRoomId)
            .delete()
            .addOnSuccessListener {
                println("채팅방 삭제 완료")
            }
            .addOnFailureListener { e ->
                println("채팅방 삭제 실패: ${e.message}")
            }
    }
}
