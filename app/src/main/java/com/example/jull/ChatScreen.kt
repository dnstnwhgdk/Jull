
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus() // 포커스 해제
                keyboardController?.hide() // 키보드 숨김
            })
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.White
                ),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center // 중앙 정렬
                    ) {
                        Text(
                            text = sellerNickname,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black // 화살표 색상
                        )
                    }
                },
                actions = { Spacer(modifier = Modifier.size(48.dp)) } // 오른쪽 공간 확보
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
                    val timestamp = message["timestamp"] as? Long ?: 0L
                    val isCurrentUser = senderId == currentUserId

                    MessageBubble(
                        content = content,
                        isCurrentUser = isCurrentUser,
                        timestamp = timestamp
                    )
                }
            }

            // 메시지 입력 필드와 전송 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {}, // 이미지만 선택하도록 필터
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Image",
                        tint = Color.Black
                    )
                }
                // 텍스트 입력 필드
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("메시지를 입력하세요...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)) // 더 둥근 모서리
                        .background(Color(0xFFF0F0F0)) // 부드러운 배경색
                        .padding(horizontal = 8.dp), // 여백 추가
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, // 포커스 시 강조 없음
                        unfocusedIndicatorColor = Color.Transparent, // 포커스 해제 시 강조 없음
                        disabledTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.width(8.dp)) // 텍스트 필드와 버튼 간의 간격

                // 전송 버튼
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
                    modifier = Modifier.size(48.dp), // 버튼 크기를 고정
                    shape = RoundedCornerShape(24.dp), // 버튼의 둥근 모서리
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                ) {
                    Text("→", color = Color.White, fontSize = 40.sp)
                }
            }

        }
    }
}

@Composable
fun MessageBubble(content: String, isCurrentUser: Boolean, timestamp: Long) {
    val timeText = remember(timestamp) {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start) {
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeText,
                style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
