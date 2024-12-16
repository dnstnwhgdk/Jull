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
            firestore.collection("chatRooms")
                .whereEqualTo("buyerId", userId)
                .addSnapshotListener { snapshot, _ ->
                    val rooms = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    _chatRooms.value = rooms
                }
        }
    }
}
