package com.example.jull

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Board() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var displayedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }  // 검색 결과를 표시할 리스트
    var searchText by remember { mutableStateOf("") }  // 검색어
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts")
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
                    displayedPosts = posts  // 초기에는 모든 게시글 표시
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("게시판") },
                    actions = {
                        // 검색 영역
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                placeholder = { Text("검색어 입력") },
                                singleLine = true,
                                modifier = Modifier
                                    .width(200.dp)
                                    .padding(end = 8.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                            )
                            // Board.kt의 검색 버튼 부분을 수정
                            Button(
                                onClick = {
                                    if (searchText.length < 2) {
                                        Toast.makeText(
                                            context,
                                            "검색어는 2글자 이상 입력해주세요",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        displayedPosts = posts  // 2글자 미만일 경우 전체 게시글 표시
                                    } else {
                                        displayedPosts = posts.filter { post ->
                                            post.title.contains(searchText, ignoreCase = true)
                                        }
                                        if (displayedPosts.isEmpty()) {
                                            Toast.makeText(
                                                context,
                                                "검색 결과가 없습니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("검색")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                )
            }
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, CreatePostActivity::class.java)
                    context.startActivity(intent)
                },
                containerColor = Color.Black,
                contentColor = Color.White,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "글쓰기")
            }
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
            displayedPosts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchText.isBlank()) "등록된 게시글이 없습니다"
                        else "검색 결과가 없습니다"
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding)
                ) {
                    items(displayedPosts) { post ->
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
fun PostItem(
    post: Post,
    onPostClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onPostClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 닉네임 표시 부분을 더 강조하여 수정
                Text(
                    text = post.authorName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,  // 글씨를 굵게
                    color = Color.Black  // 색상을 검정으로 변경
                )
                Text(
                    text = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                        .format(post.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.content,
                maxLines = 2,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "댓글 ${post.commentCount}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "좋아요 ${post.likeCount}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}