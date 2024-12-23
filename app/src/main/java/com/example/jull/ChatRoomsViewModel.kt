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

    private val _unreadMessageCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessageCounts: StateFlow<Map<String, Int>> = _unreadMessageCounts

    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount

    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        currentUserId?.let { userId ->
            firestore.collection("chatRooms")
                .whereIn("buyerId", listOf(userId)) // 현재 사용자가 구매자인 경우
                .addSnapshotListener { buyerSnapshot, buyerError ->
                    if (buyerError != null) {
                        println("Error loading buyer chat rooms: ${buyerError.message}")
                        return@addSnapshotListener
                    }

                    val buyerRooms = buyerSnapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(ChatRoom::class.java)?.copy(
                            id = doc.id,
                            imageUrl = doc.getString("itemPhotoUrl") ?: "" // 사진 URL 가져오기
                        )
                    } ?: emptyList()

                    firestore.collection("chatRooms")
                        .whereEqualTo("sellerId", userId) // 현재 사용자가 판매자인 경우
                        .addSnapshotListener { sellerSnapshot, sellerError ->
                            if (sellerError != null) {
                                println("Error loading seller chat rooms: ${sellerError.message}")
                                return@addSnapshotListener
                            }

                            val sellerRooms = sellerSnapshot?.documents?.mapNotNull { doc ->
                                doc.toObject(ChatRoom::class.java)?.copy(
                                    id = doc.id,
                                    imageUrl = doc.getString("itemPhotoUrl") ?: "" // 사진 URL 가져오기
                                )
                            } ?: emptyList()

                            // 구매자와 판매자로 참여한 채팅방 병합
                            val combinedRooms = (buyerRooms + sellerRooms).distinctBy { it.id }
                            _chatRooms.value = combinedRooms

                            // 추가 데이터 로드
                            loadLastMessagesAndNicknames(combinedRooms.map { it.id })
                            loadUnreadMessageCounts(combinedRooms.map { it.id })
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
                                val buyerId = chatRoomDoc.getString("buyerId") ?: "Unknown"

                                // 판매자와 구매자 닉네임 가져오기
                                realtimeDatabase.child("users").child(sellerId).child("nickname")
                                    .get()
                                    .addOnSuccessListener { sellerSnapshot ->
                                        val sellerNickname = sellerSnapshot.getValue(String::class.java) ?: "알 수 없음"

                                        realtimeDatabase.child("users").child(buyerId).child("nickname")
                                            .get()
                                            .addOnSuccessListener { buyerSnapshot ->
                                                val buyerNickname = buyerSnapshot.getValue(String::class.java) ?: "알 수 없음"

                                                messagesMap[chatRoomId] =
                                                    Triple(lastMessage, sellerNickname, buyerNickname)
                                                _lastMessages.value = messagesMap.toMap()
                                            }
                                    }
                            }
                    }
                }
        }
    }

    private fun loadUnreadMessageCounts(chatRoomIds: List<String>) {
        val unreadCountsMap = mutableMapOf<String, Int>()

        chatRoomIds.forEach { chatRoomId ->
            firestore.collection("chatRooms")
                .document(chatRoomId)
                .collection("messages")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Error loading unread message count for $chatRoomId: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        // 읽지 않은 메시지 필터링
                        val unreadCount = snapshot.documents.count { document ->
                            val readBy = document.get("readBy") as? List<String> ?: emptyList()
                            val senderId = document.getString("senderId")
                            !readBy.contains(currentUserId) && senderId != currentUserId // 읽지 않았고 내가 보낸 메시지가 아님
                        }
                        unreadCountsMap[chatRoomId] = unreadCount

                        // 상태 업데이트
                        _unreadMessageCounts.value = unreadCountsMap.toMap()

                        // 총 안 읽은 메시지 수 업데이트
                        _totalUnreadCount.value = _unreadMessageCounts.value.values.sum()
                    }
                }
        }
    }

    fun markMessagesAsRead(chatRoomId: String) {
        currentUserId?.let { userId ->
            firestore.collection("chatRooms")
                .document(chatRoomId)
                .collection("messages")
                .get()
                .addOnSuccessListener { snapshot ->
                    for (doc in snapshot.documents) {
                        val readBy = (doc["readBy"] as? List<String>)?.toMutableList() ?: mutableListOf()
                        val senderId = doc.getString("senderId")

                        // 읽지 않은 메시지이면서 상대방이 보낸 메시지인지 확인
                        if (!readBy.contains(userId) && senderId != userId) {
                            readBy.add(userId)
                            doc.reference.update("readBy", readBy)
                        }
                    }
                    // 읽음 처리 후 카운트 업데이트
                    loadUnreadMessageCounts(listOf(chatRoomId))
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





