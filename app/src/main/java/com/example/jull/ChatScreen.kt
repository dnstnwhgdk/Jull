

import android.net.Uri
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chatRoomId: String, onBackClick: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "UnknownUser"
    val messages = remember { mutableStateListOf<Map<String, Any>>() }
    val listState = rememberLazyListState() // 스크롤 상태 관리
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var enlargedImageUrl by remember { mutableStateOf<String?>(null) } // 팝업용 이미지 URL
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            uploadImageAndSendMessage(it, chatRoomId, currentUserId, firestore, storage)
        }
    }

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

    Column {
        Row {
                IconButton(
                    onClick = {
                        backPressedDispatcher?.onBackPressed()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text("채팅방", style = MaterialTheme.typography.titleLarge)
            }

        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
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
                    val imageUrl = message["imageUrl"] as? String

                    if (imageUrl != null) {
                        ImageBubble(
                            imageUrl = imageUrl,
                            isCurrentUser = isCurrentUser,
                            timestamp = timestamp,
                            isRead = isRead,
                            onImageClick = { enlargedImageUrl = imageUrl } // 클릭 시 이미지 URL 설정
                        )
                    } else {
                        MessageBubble(
                            content = content,
                            isCurrentUser = isCurrentUser,
                            timestamp = timestamp,
                            isRead = isRead
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = {
                        imagePickerLauncher.launch("image/*") // 이미지 선택 런처 실행
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Image",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "→")
                }
            }
        }
    }
    //이미지 팝업
    // 이미지 팝업
    if (enlargedImageUrl != null) {
        Dialog(onDismissRequest = { enlargedImageUrl = null }) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
            ) {
                AsyncImage(
                    model = enlargedImageUrl,
                    contentDescription = "Enlarged Image",
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                IconButton(
                    onClick = { enlargedImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ImageBubble(imageUrl: String, isCurrentUser: Boolean, timestamp: Long, isRead: Boolean, onImageClick: () -> Unit) {
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
                    .clickable { onImageClick() }
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Image Message",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeText,
                    style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                )
                if (isRead) {
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
                if (isRead) {
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

// 이미지 업로드 및 메시지 전송
fun uploadImageAndSendMessage(
    uri: Uri,
    chatRoomId: String,
    currentUserId: String,
    firestore: FirebaseFirestore,
    storage: FirebaseStorage
) {
    val storageRef = storage.reference.child("chatImages/${System.currentTimeMillis()}.jpg")
    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val newMessage = mapOf(
                    "senderId" to currentUserId,
                    "imageUrl" to downloadUrl.toString(),
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("chatRooms")
                    .document(chatRoomId)
                    .collection("messages")
                    .add(newMessage)
            }
        }
        .addOnFailureListener { exception ->
            println("이미지 업로드 실패: ${exception.message}")
        }
}