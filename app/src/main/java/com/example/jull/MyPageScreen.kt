package com.example.jull

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun My() {
    val tabs = listOf("내 정보", "나의 판매", "찜 목록", "알림", "내가 쓴 글", "내가 쓴 댓글")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var userInfoList by remember { mutableStateOf<List<UserInfo>>(emptyList()) }

    // Firebase Auth와 Database 인스턴스
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

    // 사용자 정보 로드
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.getReference("users/$userId")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val nickname = snapshot.child("nickname").getValue(String::class.java) ?: "정보 없음"
                        val name = snapshot.child("name").getValue(String::class.java) ?: "정보 없음"
                        val phoneNumber = snapshot.child("phonenum").getValue(String::class.java) ?: "정보 없음"
                        val email = snapshot.child("email").getValue(String::class.java) ?: "정보 없음"

                        val jyear = snapshot.child("date").child("year").getValue(Int::class.java)?.toString() ?: "정보 없음"
                        val jmonth = snapshot.child("date").child("monthValue").getValue(Int::class.java)?.toString() ?: "정보 없음"
                        val jday = snapshot.child("date").child("dayOfMonth").getValue(Int::class.java)?.toString() ?: "정보 없음"

                        val joinDate = jyear + "년 " + jmonth + "월 " + jday + "일"

                        userInfoList = listOf(
                            UserInfo("닉네임", nickname),
                            UserInfo("이름", name),
                            UserInfo("가입년도", joinDate),
                            UserInfo("전화번호", phoneNumber),
                            UserInfo("이메일", email)
                        )
                    } else {
                        Log.e("My", "사용자 데이터가 없습니다.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("My", "데이터 로드 실패: ${error.message}")
                }
            })
        } else {
            userInfoList = listOf(
                UserInfo("닉네임", "로그인 필요"),
                UserInfo("이름", "로그인 필요"),
                UserInfo("가입년도", "로그인 필요"),
                UserInfo("전화번호", "로그인 필요"),
                UserInfo("이메일", "로그인 필요")
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "마이페이지",
                        style = MaterialTheme.typography.titleLarge,  // titleLarge 유지
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)  // 하단 패딩 추가
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)  // TopAppBar 높이를 56.dp로 증가
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 4.dp,  // 가장자리 패딩 축소
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),  // 높이 약간 증가
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 0.dp)  // 좌우 패딩 약간 증가
                            .height(45.dp),
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,  // 글자 크기 조정
                                color = if (selectedTabIndex == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    )
                }
            }

            // 콘텐츠 영역
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (selectedTabIndex) {
                    0 -> ProfileScreen(userInfoList)
                    1 -> MyItem()
                    2 -> Wishlist()
                    3 -> NotificationScreen()
                    4 -> MyPosts()
                    5 -> MyComments()
                }
            }
        }
    }
}

@Composable
fun MyItem() {
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("items")
                .whereEqualTo("sellerId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        items = snapshot.documents.mapNotNull { doc ->
                            try {
                                Item.fromMap(doc.id, doc.data ?: emptyMap())
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            items.isEmpty() -> {
                Text(
                    "등록한 상품이 없습니다",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                ItemBord(
                    items = items,
                    onItemClick = { item ->
                        val intent = Intent(context, ItemDetailActivity::class.java).apply {
                            putExtra("imageUrl", item.imageUrl)
                            putExtra("title", item.title)
                            putExtra("price", item.price)
                            putExtra("brandCategory", item.brandCategory)
                            putExtra("effecterType", item.effecterType)
                            putExtra("description", item.description)
                            putExtra("sellerId", item.sellerId)
                            putExtra("id", item.id) // document ID 추가
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun Wishlist() {
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            val firestore = FirebaseFirestore.getInstance()

            // 사용자의 찜 목록 가져오기
            firestore.collection("favorites")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        // 찜한 상품들의 ID 목록
                        val itemIds = snapshot.documents.mapNotNull { it.getString("itemId") }

                        if (itemIds.isEmpty()) {
                            items = emptyList()
                            isLoading = false
                            return@addSnapshotListener
                        }

                        // 찜한 상품들의 정보 가져오기
                        firestore.collection("items")
                            .whereIn(FieldPath.documentId(), itemIds)
                            .get()
                            .addOnSuccessListener { itemsSnapshot ->
                                items = itemsSnapshot.documents.mapNotNull { doc ->
                                    try {
                                        Item.fromMap(doc.id, doc.data ?: emptyMap())
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                isLoading = false
                            }
                    }
                }
        } else {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            items.isEmpty() -> {
                Text(
                    "찜한 상품이 없습니다",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                ItemBord(
                    items = items,
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

@Composable
fun MyPosts() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        posts = snapshot.documents.mapNotNull { doc ->
                            try {
                                Post.fromMap(doc.id, doc.data ?: emptyMap())
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            posts.isEmpty() -> {
                Text(
                    "작성한 게시글이 없습니다",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                LazyColumn {
                    items(posts) { post ->
                        PostItem(
                            post = post,
                            onPostClick = {
                                val intent = Intent(context, PostDetailActivity::class.java).apply {
                                    putExtra("postId", post.id)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyComments() {
    var comments by remember { mutableStateOf<List<CommentWithPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            firestore.collection("comments")
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val commentsList = snapshot.documents.mapNotNull { doc ->
                            Comment.fromMap(doc.id, doc.data ?: emptyMap())
                        }

                        // comments 리스트 초기화
                        comments = emptyList()

                        // 각 댓글의 게시글 정보 가져오기
                        commentsList.forEach { comment ->
                            firestore.collection("posts")
                                .document(comment.postId)
                                .get()
                                .addOnSuccessListener { postDoc ->
                                    if (postDoc.exists()) {  // 게시글이 존재하는 경우에만 추가
                                        val post = Post.fromMap(postDoc.id, postDoc.data ?: emptyMap())
                                        comments = comments + CommentWithPost(comment, post)
                                    } else {
                                        // 게시글이 존재하지 않으면 해당 댓글도 삭제
                                        firestore.collection("comments")
                                            .whereEqualTo("postId", comment.postId)
                                            .get()
                                            .addOnSuccessListener { commentDocs ->
                                                commentDocs.documents.forEach { commentDoc ->
                                                    commentDoc.reference.delete()
                                                }
                                            }
                                    }
                                }
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            comments.isEmpty() -> {
                Text(
                    "작성한 댓글이 없습니다",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                LazyColumn {
                    items(comments) { commentWithPost ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    val intent = Intent(context, PostDetailActivity::class.java).apply {
                                        putExtra("postId", commentWithPost.post.id)
                                    }
                                    context.startActivity(intent)
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = commentWithPost.post.title,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = commentWithPost.comment.content)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                                        .format(commentWithPost.comment.createdAt),
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 댓글과 해당 게시글 정보를 함께 저장할 데이터 클래스
data class CommentWithPost(
    val comment: Comment,
    val post: Post
)