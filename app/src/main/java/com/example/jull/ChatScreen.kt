

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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.snapshotFlow
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
    val listState = rememberLazyListState() // 스크롤 상태 관리
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    // 실시간 메시지 로드
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
                        messages.add(doc.data ?: emptyMap())
                    }
                }
            }
    }

    // 읽음 처리: 스크롤이 메시지의 끝에 도달했을 때만 처리
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty() && visibleItems.last().index == messages.lastIndex) {
                    messages.forEachIndexed { index, message ->
                        val readBy = message["readBy"] as? List<String> ?: emptyList()
                        if (!readBy.contains(currentUserId)) {
                            firestore.collection("chatRooms")
                                .document(chatRoomId)
                                .collection("messages")
                                .document(index.toString()) // 메시지 ID에 맞게 수정 필요
                                .update("readBy", readBy + currentUserId)
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
            LazyColumn(
                state = listState, // 스크롤 상태 연결
                modifier = Modifier.weight(1f),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    val content = message["content"] as? String ?: "메시지가 없습니다"
                    val senderId = message["senderId"] as? String ?: "Unknown"
                    val timestamp = message["timestamp"] as? Long ?: 0L
                    val readBy = message["readBy"] as? List<String> ?: emptyList()
                    val isCurrentUser = senderId == currentUserId
                    val isRead = readBy.size > 1

                    MessageBubble(
                        content = content,
                        isCurrentUser = isCurrentUser,
                        timestamp = timestamp,
                        isRead = isRead
                    )
                }
            }

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
                                    "readBy" to listOf(currentUserId)
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