package com.example.jull

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    var notifications by remember { mutableStateOf<List<Item>>(emptyList()) }
    var savedKeywords by remember { mutableStateOf<List<NotificationKeyword>>(emptyList()) }
    var showAddKeywordDialog by remember { mutableStateOf(false) }
    var keywordInput by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("notificationKeywords")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        savedKeywords = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(NotificationKeyword::class.java)?.copy(id = doc.id)
                        }
                    }
                }

            FirebaseFirestore.getInstance()
                .collection("items")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        notifications = snapshot.documents.mapNotNull { doc ->
                            val item = Item.fromMap(doc.id, doc.data ?: emptyMap())
                            if (savedKeywords.any { keyword ->
                                    item.title.contains(keyword.keyword, ignoreCase = true)
                                }) {
                                item
                            } else null
                        }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)  // 전체 화면의 기본 패딩
    ) {
        // 키워드 관리 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "알림 키워드 관리",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showAddKeywordDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp)  // 버튼 내부 패딩 축소
                ) {
                    Text("키워드 추가")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                savedKeywords.forEach { keyword ->
                    AssistChip(
                        onClick = { },
                        label = { Text(keyword.keyword) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    FirebaseFirestore.getInstance()
                                        .collection("notificationKeywords")
                                        .document(keyword.id)
                                        .delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "키워드가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                                        }
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "삭제",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp, bottom = 4.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),  // border 직접 설정
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // 알림 목록
        Text(
            "알림 목록",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(notifications) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            val intent = Intent(context, ItemDetailActivity::class.java).apply {
                                putExtra("imageUrl", item.imageUrl)
                                putExtra("title", item.title)
                                putExtra("price", item.price)
                                putExtra("brandCategory", item.brandCategory)
                                putExtra("effecterType", item.effecterType)
                                putExtra("description", item.description)
                                putExtra("sellerId", item.sellerId)
                                putExtra("id", item.id)
                                putExtra("createdAt", item.createdAt)
                            }
                            context.startActivity(intent)
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "키워드가 포함된 상품이 등록되었습니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddKeywordDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddKeywordDialog = false
                keywordInput = ""
            },
            title = { Text("새 키워드 추가") },
            text = {
                OutlinedTextField(
                    value = keywordInput,
                    onValueChange = { keywordInput = it },
                    placeholder = { Text("키워드를 입력하세요") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentUser != null && keywordInput.isNotEmpty()) {
                            val keyword = NotificationKeyword(
                                userId = currentUser.uid,
                                keyword = keywordInput
                            )
                            FirebaseFirestore.getInstance()
                                .collection("notificationKeywords")
                                .add(keyword)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "키워드가 등록되었습니다", Toast.LENGTH_SHORT).show()
                                    keywordInput = ""
                                    showAddKeywordDialog = false
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "키워드 등록에 실패했습니다", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                ) {
                    Text("등록")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddKeywordDialog = false
                        keywordInput = ""
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}