package com.example.jull

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun Home() {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var selectedButtonIndex by remember { mutableStateOf(0) }
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var itemFavoriteCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("favorites")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val counts = snapshot.documents
                        .groupBy { it.getString("itemId") ?: "" }
                        .mapValues { it.value.size }
                    itemFavoriteCounts = counts
                }
            }
    }

    LaunchedEffect(selectedButtonIndex) {
        isLoading = true
        errorMessage = null

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = "데이터 로드 실패: ${e.message}"
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val loadedItems = snapshot.documents.mapNotNull { doc ->
                        try {
                            Item.fromMap(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val filteredItems = when (selectedButtonIndex) {
                        0 -> loadedItems.sortedByDescending { item ->
                            itemFavoriteCounts[item.id] ?: 0
                        }
                        1 -> loadedItems
                        2 -> loadedItems.sortedBy { it.price.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0 }
                        3 -> loadedItems.sortedByDescending { it.price.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0 }
                        else -> loadedItems
                    }

                    items = filteredItems
                    isLoading = false
                }
            }
    }

    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp)  // vertical padding 줄임
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("브랜드,상품,종류 등") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* 알람 아이콘 클릭 동작 */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = "알람")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("인기상품", "최신상품", "저가상품", "고가상품").forEachIndexed { index, text ->
                    TextButton(
                        onClick = { selectedButtonIndex = index },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (selectedButtonIndex == index) Color.Black else Color.Gray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text)
                    }
                }
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage ?: "오류가 발생했습니다")
                }
            }
            items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("등록된 상품이 없습니다")
                }
            }
            else -> {
                ItemBord(
                    items = items.filter { item ->
                        if (searchText.isEmpty()) true
                        else (item.title.contains(searchText, ignoreCase = true) ||
                                item.brandCategory.contains(searchText, ignoreCase = true) ||
                                item.effecterType.contains(searchText, ignoreCase = true))
                    },
                    onItemClick = { item ->
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
                )
            }
        }
    }
}