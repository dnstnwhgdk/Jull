
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(chatRooms) { chatRoom ->
            val lastMessageData = lastMessages[chatRoom.id]
            val lastMessage = lastMessageData?.first?.content ?: "메시지가 없습니다."
            val nickname = lastMessageData?.second ?: "알 수 없음"
            val lastMessageTime = lastMessageData?.first?.timestamp?.let {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it))
            } ?: "시간 정보 없음"

            SwipeToDeleteChatRoom(
                chatRoom = chatRoom,
                nickname = nickname,
                lastMessage = lastMessage,
                lastMessageTime = lastMessageTime,
                onChatRoomClick = { onChatRoomClick(chatRoom.id) },
                onDelete = { viewModel.deleteChatRoom(chatRoom.id) }
            )
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
    onDelete: () -> Unit
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
                        change.consume() // 이벤트 소비
                        offsetX = (offsetX + dragAmount).coerceIn(-200f, 0f) // 스와이프 제한
                    },
                    onDragEnd = {
                        // 스와이프가 일정 거리 이상이면 삭제 버튼 고정
                        if (offsetX < -100f) {
                            offsetX = -200f
                        } else {
                            offsetX = 0f // 스와이프 취소
                        }
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
                onClick = onChatRoomClick
            )
        }
    }

    // 팝업 다이얼로그
    if (showDialog) {
        DeletionConfirmationDialog(
            onConfirm = {
                showDialog = false // 팝업 닫기
                onDelete() // 삭제 동작 실행
            },
            onDismiss = {
                showDialog = false // 팝업 닫기
            }
        )
    }
}
@Composable
fun DeletionConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "삭제 확인") },
        text = { Text(text = "정말로 삭제 하겠습니까?") },
        confirmButton = {
            Button(onClick = { onConfirm() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text("예", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() },colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text("아니오", color = Color.White)
            }
        }
    )
}
@Composable
fun ChatRoomItem(
    chatRoom: ChatRoom,
    nickname: String,
    lastMessage: String,
    lastMessageTime: String,
    onClick: () -> Unit
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
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 고정 크기의 채팅방 이미지
            AsyncImage(
                model = chatRoom.imageUrl,
                contentDescription = "채팅방 사진",
                modifier = Modifier
                    .size(80.dp) // 크기를 고정하여 일정하게 유지
                    .clip(MaterialTheme.shapes.medium) // 이미지 모서리를 둥글게 설정 (선택 사항)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 판매자 닉네임
                Text(
                    text = "판매자: $nickname",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 마지막 메시지
                Text(
                    text = lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1 // 한 줄만 표시
                )

                // 마지막 메시지 시간
                Text(
                    text = lastMessageTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

