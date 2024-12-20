
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chatRoomId: String, onBackClick: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val realtimeDatabase = FirebaseDatabase.getInstance().reference
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "UnknownUser"

    val messages = remember { mutableStateListOf<Map<String, Any>>() }
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var sellerNickname by remember { mutableStateOf("판매자 정보 로딩 중...") }

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

        // 판매자 닉네임 로드
        firestore.collection("chatRooms")
            .document(chatRoomId)
            .get()
            .addOnSuccessListener { chatRoomDoc ->
                val sellerId = chatRoomDoc.getString("sellerId")
                if (!sellerId.isNullOrEmpty()) {
                    // Realtime Database에서 판매자 닉네임 가져오기
                    realtimeDatabase.child("users").child(sellerId).child("nickname")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                sellerNickname = snapshot.getValue(String::class.java) ?: "알 수 없음"
                            }

                            override fun onCancelled(error: DatabaseError) {
                                sellerNickname = "닉네임 로드 실패"
                                println("Realtime Database 오류: ${error.message}")
                            }
                        })
                }
            }
    }

    // UI 레이아웃
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "채팅방 - $sellerNickname",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    Button(
                        onClick = { onBackClick() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text("←", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 메시지 리스트
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                reverseLayout = false
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
                        .background(Color(0xFFF5F5F5))
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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("Send")
                }
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
