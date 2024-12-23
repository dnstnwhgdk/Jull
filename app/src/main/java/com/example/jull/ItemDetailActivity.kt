package com.example.jull

import ChatScreen
import ItemDetailViewModel
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.input.KeyboardType

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
        val itemId = intent.getStringExtra("id") ?: ""
        val tradeType = intent.getStringExtra("tradeType") ?: "택배거래"
        val createdAt = (intent.getSerializableExtra("createdAt") as? Date) ?: Date()

        setContent {
            CppNavigation(
                imageUrl = imageUrl,
                title = title,
                price = price,
                brandCategory = brandCategory,
                effecterType = effecterType,
                description = description,
                tradeType = tradeType,
                sellerId = sellerId,
                itemId = itemId,
                createdAt = createdAt,
                onBackPressed = { finish() }
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
    tradeType: String,
    createdAt: Date,
    onBackPressed: () -> Unit
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
                createdAt = createdAt,
                tradeType = tradeType,
                onBackPressed = onBackPressed,
                onChatNavigate = { chatRoomId ->
                    navController.navigate("chat/$chatRoomId")
                }
            )
        }

        composable("chat/{chatRoomId}") { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
            ChatScreen(chatRoomId, onBackPressed)
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
    itemId: String,
    createdAt: Date,
    onBackPressed: () -> Unit,
    viewModel: ItemDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onChatNavigate: (String) -> Unit,
    status: String = "판매중",
    tradeType: String
) {
    val imageUrls = remember(imageUrl) { imageUrl.split(",") }
    val pagerState = rememberPagerState { imageUrls.size }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var isFavorite by remember { mutableStateOf(false) }
    var favoriteCount by remember { mutableStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(title) }
    var editPrice by remember { mutableStateOf(price) }
    var editDescription by remember { mutableStateOf(description) }

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            FirebaseFirestore.getInstance()
                .collection("favorites")
                .document("${currentUserId}_$itemId")
                .addSnapshotListener { snapshot, _ ->
                    isFavorite = snapshot?.exists() == true
                }

            FirebaseFirestore.getInstance()
                .collection("favorites")
                .whereEqualTo("itemId", itemId)
                .addSnapshotListener { snapshot, _ ->
                    favoriteCount = snapshot?.documents?.size ?: 0
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
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        IconButton(
                            onClick = {
                                if (currentUserId != null) {
                                    val favoriteRef = FirebaseFirestore.getInstance()
                                        .collection("favorites")
                                        .document("${currentUserId}_$itemId")

                                    if (isFavorite) {
                                        favoriteRef.delete()
                                            .addOnSuccessListener {
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
                                contentDescription = "찜하기",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
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
                    }
                    Column {
                        Text(
                            text = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                                .format(createdAt),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = when (tradeType) {
                                "택배거래" -> "택배거래"
                                "직거래" -> "직거래"
                                "택배거래/직거래" -> "택배거래/직거래"
                                else -> tradeType
                            },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (price.endsWith("원")) price else price + "원",
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
                    Column {
                        Button(
                            onClick = {
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
                                                .addOnFailureListener { exception ->
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

                        Spacer(modifier = Modifier.height(8.dp))

                        var expanded by remember { mutableStateOf(false) }
                        var selectedStatus by remember { mutableStateOf(status) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedStatus,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Black,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("판매중", "예약중", "판매완료").forEach { statusOption ->
                                    DropdownMenuItem(
                                        text = { Text(statusOption) },
                                        onClick = {
                                            selectedStatus = statusOption
                                            expanded = false
                                            val firestore = FirebaseFirestore.getInstance()
                                            firestore.collection("items").document(itemId)
                                                .update("status", statusOption)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "상태가 변경되었습니다", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(context, "상태 변경 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 수정하기와 삭제하기 버튼
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("수정하기")
                            }

                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("삭제하기")
                            }
                        }

                        // 수정 다이얼로그
                        if (showEditDialog) {
                            AlertDialog(
                                onDismissRequest = { showEditDialog = false },
                                title = { Text("상품 정보 수정") },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = editTitle,
                                            onValueChange = { editTitle = it },
                                            label = { Text("제목") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        )

                                        OutlinedTextField(
                                            value = editPrice,
                                            onValueChange = { editPrice = it },
                                            label = { Text("가격") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        )

                                        OutlinedTextField(
                                            value = editDescription,
                                            onValueChange = { editDescription = it },
                                            label = { Text("설명") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .padding(vertical = 4.dp),
                                            maxLines = 5
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            firestore.collection("items").document(itemId)
                                                .update(mapOf(
                                                    "title" to editTitle,
                                                    "price" to editPrice,
                                                    "description" to editDescription
                                                ))
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "상품 정보가 수정되었습니다", Toast.LENGTH_SHORT).show()
                                                    showEditDialog = false
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(context, "수정 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                    ) {
                                        Text("수정")
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = { showEditDialog = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) {
                                        Text("취소")
                                    }
                                }
                            )
                        }

                        // 삭제 확인 대화상자
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("삭제 확인") },
                                text = { Text("정말로 삭제하시겠습니까?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val firestore = FirebaseFirestore.getInstance()
                                            firestore.collection("items").document(itemId)
                                                .delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "상품이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                                                    onBackPressed()
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(context, "삭제 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            showDeleteDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                    ) {
                                        Text("삭제")
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = { showDeleteDialog = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) {
                                        Text("취소")
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.findOrCreateChatRoom(
                                itemId = title,
                                sellerId = sellerId,
                                buyerId = currentUserId ?:"",
                                onChatRoomFound = { chatRoomId ->
                                    Toast.makeText(context, "채팅방 이동: $chatRoomId", Toast.LENGTH_SHORT).show()
                                    onChatNavigate(chatRoomId)
                                },
                                itemPhotoUrl = imageUrl,
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