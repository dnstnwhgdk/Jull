package com.example.jull

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

private fun getTimeAgo(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        seconds < 60 -> "방금"
        minutes < 60 -> "${minutes}분 전"
        hours < 24 -> "${hours}시간 전"
        days < 7 -> "${days}일 전"
        weeks < 4 -> "${weeks}주 전"
        months < 12 -> "${months}달 전"
        else -> "${years}년 전"
    }
}

@Composable
fun ItemBord(
    items: List<Item>,
    onItemClick: (Item) -> Unit = {}
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(items.chunked(1)) { rowItems ->
            Row {
                rowItems.forEach { item ->
                    var isFavorite by remember { mutableStateOf(false) }
                    var favoriteCount by remember { mutableStateOf(0) }

                    LaunchedEffect(item.id, currentUserId) {
                        if (currentUserId != null) {
                            firestore.collection("favorites")
                                .document("${currentUserId}_${item.id}")
                                .get()
                                .addOnSuccessListener { document ->
                                    isFavorite = document.exists()
                                }
                        }

                        firestore.collection("favorites")
                            .whereEqualTo("itemId", item.id)
                            .get()
                            .addOnSuccessListener { documents ->
                                favoriteCount = documents.size()
                            }
                    }

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .wrapContentSize()
                            .clickable {
                                onItemClick(item.copy(
                                    tradeType = item.tradeType
                                ))
                            },
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AsyncImage(
                                        model = item.imageUrl.split(",").firstOrNull(),
                                        contentDescription = "상품 이미지",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = getTimeAgo(item.createdAt),
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .background(
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .padding(4.dp)
                                        )

                                        if (item.status == "예약중") {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "예약중",
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .background(
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        shape = MaterialTheme.shapes.small
                                                    )
                                                    .padding(4.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (item.tradeType) {
                                                "택배거래" -> "택"
                                                "직거래" -> "직"
                                                "택배거래/직거래" -> "택/직"
                                                else -> ""
                                            },
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .background(
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .padding(4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        item.title,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Text(item.brandCategory)
                                Text(item.effecterType)
                                Text(
                                    if (item.price.endsWith("원")) item.price else item.price + "원",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = favoriteCount.toString(),
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(end = 2.dp)
                                )

                                IconButton(
                                    onClick = {
                                        if (currentUserId != null) {
                                            val favoriteRef = firestore.collection("favorites")
                                                .document("${currentUserId}_${item.id}")

                                            if (isFavorite) {
                                                favoriteRef.delete()
                                                    .addOnSuccessListener {
                                                        isFavorite = false
                                                        favoriteCount--
                                                        Toast.makeText(context, "찜 목록에서 제거되었습니다", Toast.LENGTH_SHORT).show()
                                                    }
                                            } else {
                                                val favorite = hashMapOf(
                                                    "userId" to currentUserId,
                                                    "itemId" to item.id,
                                                    "createdAt" to Date()
                                                )
                                                favoriteRef.set(favorite)
                                                    .addOnSuccessListener {
                                                        isFavorite = true
                                                        favoriteCount++
                                                        Toast.makeText(context, "찜 목록에 추가되었습니다", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        } else {
                                            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (isFavorite) "Favorite" else "Not Favorite",
                                        tint = if (isFavorite) Color.Red else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}