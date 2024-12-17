import androidx.lifecycle.ViewModel
import com.example.jull.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatRoomsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        currentUserId?.let { userId ->
            // Firestore에서 buyerId 또는 sellerId가 현재 사용자 ID와 일치하는 채팅방 조회
            firestore.collection("chatRooms")
                .whereIn("buyerId", listOf(userId)) // 현재 사용자가 구매자인 경우
                .addSnapshotListener { snapshot, _ ->
                    val buyerRooms = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    firestore.collection("chatRooms")
                        .whereEqualTo("sellerId", userId) // 현재 사용자가 판매자인 경우
                        .addSnapshotListener { sellerSnapshot, _ ->
                            val sellerRooms = sellerSnapshot?.documents?.mapNotNull { doc ->
                                doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                            } ?: emptyList()

                            // 중복된 채팅방 제거 (buyer와 seller 모두에 동일한 채팅방이 나타나지 않도록)
                            val combinedRooms = (buyerRooms + sellerRooms).distinctBy { it.id }
                            _chatRooms.value = combinedRooms
                        }
                }
        }
    }
}
