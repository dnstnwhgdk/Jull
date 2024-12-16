package com.example.jull

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(chatRoomId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "UnknownUser"

    val messages = remember { mutableStateListOf<Map<String, Any>>() }
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

    // UI 레이아웃
    Column(modifier = Modifier.fillMaxSize()) {
        // 메시지 리스트
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = false // 최신 메시지가 아래로 가도록 설정
        ) {
            items(messages) { message ->
                val content = message["content"] as? String ?: "메시지가 없습니다"
                val senderId = message["senderId"] as? String ?: "Unknown"
                val isCurrentUser = senderId == currentUserId

                MessageBubble(content = content, isCurrentUser = isCurrentUser)
            }
        }

        // 메시지 입력 필드와 전송 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
                    .padding(horizontal = 8.dp),
                placeholder = { Text("메시지를 입력하세요...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (messageText.text.isNotBlank()) {
                        scope.launch {
                            val newMessage = mapOf(
                                "senderId" to currentUserId,
                                "content" to messageText.text,
                                "timestamp" to System.currentTimeMillis()
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
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageBubble(content: String, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isCurrentUser) Color(0xFFDCF8C6) else Color(0xFFE5E5EA))
                .padding(12.dp)
        ) {
            Text(
                text = content,
                style = TextStyle(fontSize = 16.sp, color = Color.Black),
                fontWeight = FontWeight.Normal
            )
        }
    }
}
