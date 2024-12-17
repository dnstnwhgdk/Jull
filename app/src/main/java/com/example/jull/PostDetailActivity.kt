package com.example.jull

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class PostDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val postId = intent.getStringExtra("postId") ?: ""
        setContent {
            PostDetailScreen(
                postId = postId,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(postId: String, onBack: () -> Unit) {
    var post by remember { mutableStateOf<Post?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    LaunchedEffect(postId) {
        firestore.collection("posts")
            .document(postId)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null && snapshot.exists()) {
                    post = Post.fromMap(postId, snapshot.data ?: emptyMap())
                }
                isLoading = false
            }

        currentUserId?.let { uid ->
            firestore.collection("postLikes")
                .document("${uid}_${postId}")
                .addSnapshotListener { snapshot, _ ->
                    isLiked = snapshot?.exists() == true
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            post == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("게시글을 찾을 수 없습니다")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = post!!.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = post!!.authorName,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                                    .format(post!!.createdAt),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = post!!.content,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (currentUserId == null) {
                                            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                                            return@IconButton
                                        }

                                        val likeRef = firestore.collection("postLikes")
                                            .document("${currentUserId}_${postId}")

                                        if (isLiked) {
                                            likeRef.delete()
                                            firestore.collection("posts")
                                                .document(postId)
                                                .update("likeCount", post!!.likeCount - 1)
                                        } else {
                                            likeRef.set(mapOf(
                                                "userId" to currentUserId,
                                                "postId" to postId,
                                                "createdAt" to Date()
                                            ))
                                            firestore.collection("posts")
                                                .document(postId)
                                                .update("likeCount", post!!.likeCount + 1)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "좋아요",
                                        tint = if (isLiked) Color.Red else Color.Gray
                                    )
                                }
                                Text(
                                    text = post!!.likeCount.toString(),
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "댓글 ${post!!.commentCount}",
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 댓글 입력 부분
                    item {
                        var commentText by remember { mutableStateOf("") }

                        Column {
                            Text(
                                text = "댓글",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    placeholder = { Text("댓글을 입력하세요") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (commentText.isBlank()) return@Button
                                        if (currentUserId == null) {
                                            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        firestore.collection("users")
                                            .document(currentUserId)
                                            .get()
                                            .addOnSuccessListener { document ->
                                                val nickname = document.getString("nickname") ?: "익명"

                                                val comment = Comment(
                                                    postId = postId,
                                                    userId = currentUserId,
                                                    authorName = nickname,
                                                    content = commentText,
                                                    createdAt = Date()
                                                )

                                                firestore.collection("comments")
                                                    .add(comment.toMap())
                                                    .addOnSuccessListener {
                                                        firestore.collection("posts")
                                                            .document(postId)
                                                            .update("commentCount", (post?.commentCount ?: 0) + 1)

                                                        commentText = ""
                                                    }
                                            }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                ) {
                                    Text("등록")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 댓글 목록
                    item {
                        var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }

                        LaunchedEffect(postId) {
                            firestore.collection("comments")
                                .whereEqualTo("postId", postId)
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .addSnapshotListener { snapshot, _ ->
                                    if (snapshot != null) {
                                        comments = snapshot.documents.mapNotNull { doc ->
                                            Comment.fromMap(doc.id, doc.data ?: emptyMap())
                                        }
                                    }
                                }
                        }

                        if (comments.isNotEmpty()) {
                            comments.forEach { comment ->
                                CommentItem(
                                    comment = comment,
                                    currentUserId = currentUserId,
                                    onDelete = { commentId ->
                                        firestore.collection("comments")
                                            .document(commentId)
                                            .delete()
                                            .addOnSuccessListener {
                                                firestore.collection("posts")
                                                    .document(postId)
                                                    .update("commentCount", (post?.commentCount ?: 1) - 1)
                                            }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.authorName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (comment.userId == currentUserId) {
                    TextButton(
                        onClick = { onDelete(comment.id) }
                    ) {
                        Text(
                            text = "삭제",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                    .format(comment.createdAt),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}