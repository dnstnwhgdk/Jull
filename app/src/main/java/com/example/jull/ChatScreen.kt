
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chatRoomId: String, onBackClick: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "UnknownUser"
    val messages = remember { mutableStateListOf<Map<String, Any>>() }
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    // 실시간 메시지 로드 및 읽음 처리
    LaunchedEffect(chatRoomId) {
        firestore.collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    println("Firestore 오류: ${exception.message}")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    messages.clear()
                    for (doc in it.documents) {
                        val message = doc.data ?: emptyMap()
                        messages.add(message)

                        // 메시지 읽음 처리
                        val readBy = message["readBy"] as? List<String> ?: emptyList()
                        if (!readBy.contains(currentUserId)) {
                            doc.reference.update("readBy", readBy + currentUserId)
                        }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("채팅방") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 메시지 리스트
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    val content = message["content"] as? String ?: "메시지가 없습니다"
                    val senderId = message["senderId"] as? String ?: "Unknown"
                    val timestamp = message["timestamp"] as? Long ?: 0L
                    val readBy = message["readBy"] as? List<String> ?: emptyList()
                    val isCurrentUser = senderId == currentUserId
                    val isRead = readBy.size > 1 // 나를 제외한 다른 사용자가 읽었는지 확인

                    MessageBubble(
                        content = content,
                        isCurrentUser = isCurrentUser,
                        timestamp = timestamp,
                        isRead = isRead
                    )
                }
            }

            // 메시지 입력 필드와 전송 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("메시지를 입력하세요...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (messageText.text.isNotBlank()) {
                            scope.launch {
                                val newMessage = mapOf(
                                    "senderId" to currentUserId,
                                    "content" to messageText.text,
                                    "timestamp" to System.currentTimeMillis(),
                                    "readBy" to listOf(currentUserId) // 메시지 전송 시 나를 읽음 처리
                                )
                                firestore.collection("chatRooms")
                                    .document(chatRoomId)
                                    .collection("messages")
                                    .add(newMessage)
                                messageText = TextFieldValue("") // 입력 필드 초기화
                            }
                        }
                    }
                ) {
                    Text("보내기")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(content: String, isCurrentUser: Boolean, timestamp: Long, isRead: Boolean) {
    val timeText = remember(timestamp) {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCurrentUser) Color(0xFFDCF8C6) else Color(0xFFE5E5EA))
                    .padding(12.dp)
            ) {
                Text(text = content)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeText,
                    style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                )
                if (isCurrentUser && isRead) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "읽음",
                        style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                    )
                }
            }
        }
    }
}