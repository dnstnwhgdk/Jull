package com.example.jull

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(chatRoomId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val messages = remember { mutableStateListOf<Map<String, Any>>() }
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    // 채팅 메시지 로드
    LaunchedEffect(chatRoomId) {
        firestore.collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    messages.clear()
                    messages.addAll(it.documents.map { doc -> doc.data ?: emptyMap() })
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 메시지 리스트
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            messages.forEach { message ->
                val content = message["content"] as? String ?: ""
                val senderId = message["senderId"] as? String ?: ""
                Text("$senderId: $content")
            }
        }

        // 입력 필드 및 전송 버튼
        Row(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (messageText.text.isNotBlank()) {
                    scope.launch {
                        val newMessage = mapOf(
                            "senderId" to FirebaseAuth.getInstance().currentUser?.uid,
                            "content" to messageText.text,
                            "timestamp" to System.currentTimeMillis()
                        )
                        firestore.collection("chatRooms")
                            .document(chatRoomId)
                            .collection("messages")
                            .add(newMessage)
                        messageText = TextFieldValue("")
                    }
                }
            }) {
                Text("Send")
            }
        }
    }
}
