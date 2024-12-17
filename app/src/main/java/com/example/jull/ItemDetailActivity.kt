package com.example.jull

import ItemDetailViewModel
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class ItemDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val price = intent.getStringExtra("price") ?: ""
        val brandCategory = intent.getStringExtra("brandCategory") ?: ""
        val effecterType = intent.getStringExtra("effecterType") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val sellerId = intent.getStringExtra("sellerId") ?: ""
        val id = intent.getStringExtra("id") ?: ""

        setContent {
            CppNavigation(
                imageUrl = imageUrl,
                title = title,
                price = price,
                brandCategory = brandCategory,
                effecterType = effecterType,
                description = description,
                sellerId = sellerId,
                itemId = id,
                onBackPressed = { finish() } // Activity 종료 콜백 추가
            )
        }
    }
}

@Composable
fun CppNavigation(
    imageUrl: String,
    title: String,
    price: String,
    brandCategory: String,
    effecterType: String,
    description: String,
    sellerId: String,
    itemId: String,
    onBackPressed: () -> Unit // 추가된 파라미터
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "itemDetail") {
        composable("itemDetail") {
            ItemDetailScreen(
                imageUrl = imageUrl,
                title = title,
                price = price,
                brandCategory = brandCategory,
                effecterType = effecterType,
                description = description,
                sellerId = sellerId,
                itemId = itemId,
                onBackPressed = onBackPressed, // Activity의 onBackPressed 전달
                onChatNavigate = { chatRoomId ->
                    navController.navigate("chat/$chatRoomId")
                }
            )
        }
        // ... 나머지 코드
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    imageUrl: String,
    title: String,
    price: String,
    brandCategory: String,
    effecterType: String,
    description: String,
    sellerId: String,
    itemId: String,
    onBackPressed: () -> Unit,
    viewModel: ItemDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onChatNavigate: (String) -> Unit
) {
    val imageUrls = remember(imageUrl) { imageUrl.split(",") }
    val pagerState = rememberPagerState { imageUrls.size }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var isFavorite by remember { mutableStateOf(false) }
    var favoriteCount by remember { mutableStateOf(0) }

    LaunchedEffect(itemId, currentUserId) {
        if (currentUserId != null) {
            firestore.collection("favorites")
                .document("${currentUserId}_${itemId}")
                .get()
                .addOnSuccessListener { document ->
                    isFavorite = document.exists()
                }

            firestore.collection("favorites")
                .whereEqualTo("itemId", itemId)
                .get()
                .addOnSuccessListener { documents ->
                    favoriteCount = documents.size()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("상품 상세") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = favoriteCount.toString(),
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        IconButton(
                            onClick = {
                                if (currentUserId != null) {
                                    val favoriteRef = firestore.collection("favorites")
                                        .document("${currentUserId}_${itemId}")

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
                                            "itemId" to itemId,
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
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFavorite) "찜 해제" else "찜하기",
                                tint = if (isFavorite) Color.Red else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = imageUrls[page],
                        contentDescription = "상품 이미지 ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                if (imageUrls.size > 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(imageUrls.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(8.dp)
                                        .background(
                                            color = if (pagerState.currentPage == index)
                                                Color.White
                                            else
                                                Color.White.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = brandCategory,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = effecterType,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = price,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color.LightGray, thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = description,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (currentUserId == sellerId) {
                    Button(
                        onClick = {
                            firestore.collection("items")
                                .whereEqualTo("sellerId", sellerId)
                                .whereEqualTo("title", title)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        document.reference.update("createdAt", Date())
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "상품이 끌어올려졌습니다", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "끌어올리기 실패", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("물건 끌어올리기")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.findOrCreateChatRoom(
                                itemId = itemId,
                                sellerId = sellerId,
                                buyerId = currentUserId ?: "",
                                onChatRoomFound = { chatRoomId ->
                                    Toast.makeText(context, "채팅방 이동: $chatRoomId", Toast.LENGTH_SHORT).show()
                                    onChatNavigate(chatRoomId)
                                },
                                onError = { exception ->
                                    Toast.makeText(context, "채팅방 생성 오류: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("채팅")
                    }
                }
            }
        }
    }
}