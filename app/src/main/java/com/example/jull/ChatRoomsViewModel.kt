import androidx.lifecycle.ViewModel
import com.example.jull.ChatRoom
import com.example.jull.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatRoomsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _lastMessages = MutableStateFlow<Map<String, Pair<Message, String>>>(emptyMap())
    val lastMessages: StateFlow<Map<String, Pair<Message, String>>> = _lastMessages

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

                            // Load last messages and nicknames
                            loadLastMessages(combinedRooms.map { it.id }, userId)
                        }
                }
        }
    }

    private fun loadLastMessages(chatRoomIds: List<String>, currentUserId: String) {
        val messagesMap = mutableMapOf<String, Pair<Message, String>>()

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
                                val otherUserId =
                                    if (chatRoomDoc.getString("buyerId") == currentUserId) {
                                        chatRoomDoc.getString("sellerId")
                                    } else {
                                        chatRoomDoc.getString("buyerId")
                                    }

                                firestore.collection("users")
                                    .document(otherUserId ?: "")
                                    .get()
                                    .addOnSuccessListener { userDoc ->
                                        val nickname = userDoc.getString("nickname") ?: "알 수 없음"
                                        messagesMap[chatRoomId] = lastMessage to nickname
                                        _lastMessages.value = messagesMap.toMap()
                                    }
                            }
                    }
                }
        }
    }
}
