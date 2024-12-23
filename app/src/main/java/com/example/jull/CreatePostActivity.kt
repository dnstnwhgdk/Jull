package com.example.jull

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreatePostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreatePostScreen(onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("글쓰기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                Toast.makeText(context, "제목과 내용을 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            isLoading = true
                            val firestore = FirebaseFirestore.getInstance()

                            // 현재 사용자의 닉네임 가져오기
                            if (currentUser != null) {
                                // 먼저 Firebase Realtime Database에서 닉네임 가져오기 시도
                                FirebaseDatabase.getInstance().reference
                                    .child("users")
                                    .child(currentUser.uid)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        val userNickname = snapshot.child("nickname").getValue(String::class.java)
                                        if (userNickname != null) {
                                            val post = Post(
                                                userId = currentUser.uid,
                                                title = title,
                                                content = content,
                                                authorName = userNickname,
                                                createdAt = Date()
                                            )

                                            firestore.collection("posts")
                                                .add(post.toMap())
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "게시글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                                                    onBack()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    isLoading = false
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "사용자 정보를 가져오는데 실패했습니다", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                    }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("등록")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                enabled = !isLoading
            )
        }
    }
}