package com.example.jull

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.accompanist.flowlayout.FlowRow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    var notifications by remember { mutableStateOf<List<Item>>(emptyList()) }
    var keywords by remember { mutableStateOf<List<NotificationKeyword>>(emptyList()) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            // 사용자의 키워드 가져오기
            FirebaseFirestore.getInstance()
                .collection("notificationKeywords")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        keywords = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(NotificationKeyword::class.java)?.copy(id = doc.id)
                        }
                    }
                }

            // 새로운 상품 모니터링
            FirebaseFirestore.getInstance()
                .collection("items")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        notifications = snapshot.documents.mapNotNull { doc ->
                            val item = Item.fromMap(doc.id, doc.data ?: emptyMap())
                            if (keywords.any { keyword ->
                                    item.title.contains(keyword.keyword, ignoreCase = true)
                                }) {
                                item
                            } else null
                        }
                    }
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 등록된 키워드 표시
        Text(
            "등록된 키워드",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        FlowRow(modifier = Modifier.padding(horizontal = 16.dp)) {
            keywords.forEach { keyword ->
                AssistChip(
                    onClick = { },
                    label = { Text(keyword.keyword) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // 알림 목록 표시
        Text(
            "알림 목록",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn {
            items(notifications) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        // clickable 대신 Card를 클릭가능하게 만들기
                        .clickable {
                            // ItemDetailActivity로 이동하는 Intent 생성
                            val intent = Intent(context, ItemDetailActivity::class.java).apply {
                                putExtra("imageUrl", item.imageUrl)
                                putExtra("title", item.title)
                                putExtra("price", item.price)
                                putExtra("brandCategory", item.brandCategory)
                                putExtra("effecterType", item.effecterType)
                                putExtra("description", item.description)
                                putExtra("sellerId", item.sellerId)
                                putExtra("id", item.id)
                            }
                            context.startActivity(intent)
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "키워드가 포함된 상품이 등록되었습니다",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            item.price,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                .format(item.createdAt),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}