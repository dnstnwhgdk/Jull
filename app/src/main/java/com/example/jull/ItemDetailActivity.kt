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
        val itemId = intent.getStringExtra("itemId") ?: ""
        val createdAt = intent.getStringExtra("createdAt") ?: ""

        setContent {
            CppNavigation(imageUrl, title, price, brandCategory, effecterType, description, sellerId)
        }
    }
}
@Composable
fun CppNavigation(imageUrl: String, title: String, price: String, brandCategory: String, effecterType: String, description: String, sellerId: String,) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "itemDetail") {
        // 상품 상세 화면 라우트
        composable("itemDetail") {
            ItemDetailScreen(
                imageUrl = imageUrl,
                title = title,
                price = price,
                brandCategory = brandCategory,
                effecterType = effecterType,
                description = description,
                sellerId = sellerId,
                onBackPressed = { navController.popBackStack() },
                onChatNavigate = { chatRoomId ->
                    navController.navigate("chat/$chatRoomId")
                },
                )
        }

        // 채팅 화면 라우트
        composable("chat/{chatRoomId}") { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
            ChatScreen(chatRoomId)
        }
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
    onBackPressed: () -> Unit,
    viewModel: ItemDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onChatNavigate: (String) -> Unit
) {
    val imageUrls = remember(imageUrl) { imageUrl.split(",") }
    val pagerState = rememberPagerState { imageUrls.size }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("상품 상세") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
                // 이미지 페이저
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

                // 페이지 인디케이터
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

            // 상품 정보
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 브랜드 카테고리 표시
                Text(
                    text = brandCategory,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // 이펙터 유형 표시
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

                // 버튼 영역
                if (currentUserId == sellerId) {
                    // 판매자가 자신의 상품을 볼 때
                    Button(
                        onClick = {
                            // 끌어올리기 기능 구현
                            val firestore = FirebaseFirestore.getInstance()
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
                    // 다른 사용자가 상품을 볼 때
                    Button(
                        onClick = {
                            viewModel.findOrCreateChatRoom(
                                itemId = title, // itemId가 필요하므로 title 대체 가능
                                sellerId = sellerId,
                                buyerId = currentUserId ?:"",
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
