
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.jull.ChatRoom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

            ChatRoomItem(
                chatRoom = chatRoom,
                nickname = nickname, // 판매자 닉네임 전달
                lastMessage = lastMessage,
                lastMessageTime = lastMessageTime,
                onClick = { onChatRoomClick(chatRoom.id) }
            )
        }
    }
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


