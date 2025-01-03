import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.jull.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ChatRoomsScreen(
    onChatRoomClick: (String) -> Unit,
    viewModel: ChatRoomsViewModel = viewModel()
) {
    val chatRooms by viewModel.chatRooms.collectAsState()
    val lastMessages by viewModel.lastMessages.collectAsState()
    val unreadCounts by viewModel.unreadMessageCounts.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 제목
        Text(
            text = "채팅목록",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )

        if (chatRooms.isEmpty()) {
            // 채팅방이 없을 시 표시되는 문구
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "채팅중인 채팅방이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatRooms) { chatRoom ->
                    val lastMessageData = lastMessages[chatRoom.id]
                    val unreadCount = unreadCounts[chatRoom.id] ?: 0
                    val lastMessage = lastMessageData?.first?.content ?: "메시지가 없습니다."
                    val nickname = if (chatRoom.sellerId == currentUserId) {
                        "구매요청: ${lastMessageData?.third ?: "알 수 없음"}"
                    } else {
                        "판매자: ${lastMessageData?.second ?: "알 수 없음"}"
                    }
                    val lastMessageTime = lastMessageData?.first?.timestamp?.let {
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it))
                    } ?: "시간 정보 없음"

                    SwipeToDeleteChatRoom(
                        chatRoom = chatRoom,
                        nickname = nickname,
                        lastMessage = lastMessage,
                        lastMessageTime = lastMessageTime,
                        onChatRoomClick = {
                            viewModel.markMessagesAsRead(chatRoom.id)
                            onChatRoomClick(chatRoom.id)
                        },
                        onDelete = { viewModel.deleteChatRoom(chatRoom.id) },
                        isSeller = chatRoom.sellerId == currentUserId,
                        newMessageCount = unreadCount
                    )
                }
            }
        }
    }
}



@Composable
fun SwipeToDeleteChatRoom(
    chatRoom: ChatRoom,
    nickname: String,
    lastMessage: String,
    lastMessageTime: String,
    onChatRoomClick: () -> Unit,
    onDelete: () -> Unit,
    isSeller: Boolean,
    newMessageCount: Int
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount).coerceIn(-200f, 0f)
                    },
                    onDragEnd = {
                        offsetX = if (offsetX < -100f) -200f else 0f
                    }
                )
            }
    ) {
        // 삭제 버튼
        if (offsetX <= -100f) {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .width(80.dp)
                    .align(Alignment.CenterEnd),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("삭제", color = Color.White)
            }
        }

        // 채팅방 카드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
        ) {
            ChatRoomItem(
                chatRoom = chatRoom,
                nickname = nickname,
                lastMessage = lastMessage,
                lastMessageTime = lastMessageTime,
                onClick = onChatRoomClick,
                isSeller = isSeller,
                newMessageCount = newMessageCount
            )
        }
    }

    // 삭제 확인 다이얼로그
    if (showDialog) {
        DeletionConfirmationDialog(
            onConfirm = {
                showDialog = false
                onDelete()
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
}

@Composable
fun ChatRoomItem(
    chatRoom: ChatRoom,
    nickname: String,
    lastMessage: String,
    lastMessageTime: String,
    newMessageCount: Int,
    onClick: () -> Unit,
    isSeller: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(80.dp)) {
                    AsyncImage(
                        model = chatRoom.imageUrl,
                        contentDescription = "채팅방 사진",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    if (newMessageCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Red, shape = MaterialTheme.shapes.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = newMessageCount.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Text(
                    text = if (isSeller) "판매물품" else "구매물품",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSeller) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
                Text(
                    text = lastMessageTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}


@Composable
fun DeletionConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "게시글 삭제",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "정말 삭제하시겠습니까?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text(
                    text = "삭제",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(
                    text = "취소",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        shape = MaterialTheme.shapes.medium,
        backgroundColor = MaterialTheme.colorScheme.surface,
    )
}
