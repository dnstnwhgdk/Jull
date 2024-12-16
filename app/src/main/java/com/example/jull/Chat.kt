package com.example.jull

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jull.ui.theme.JullTheme

@Composable
fun Chat() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "chatList") {
        composable("chatList") { ChatListScreen(navController) }
        composable("chatRoom/{chatName}") { backStackEntry ->
            val chatName = backStackEntry.arguments?.getString("chatName") ?: "Unknown"
            ChatRoomScreen(chatName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavHostController) {
    val chatItems = remember {
        listOf(
            ChatItem("친구1", "안녕!", "오후 2:45"),
            ChatItem("친구2", "오늘 저녁 어때?", "어제"),
            ChatItem("친구3", "고마워요!", "3일 전"),
            ChatItem("친구4", "사진 보냈어요.", "1주 전")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("채팅방 목록", color = Color.Black) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White)
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(padding)
            ) {
                items(chatItems.size) { chatItem ->
                    ChatListItem(chatItems[chatItem]) {
                        navController.navigate("chatRoom/${chatItems[chatItem].name}")
                    }
                }
            }
        }
    )
}

@Composable
fun ChatListItem(chatItem: ChatItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray, shape = MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chatItem.name.first().toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chatItem.name,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = chatItem.lastMessage,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
        Text(
            text = chatItem.timestamp,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(chatName: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatName, color = Color.Black) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White)
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(padding)
            ) {
                Text(
                    text = "$chatName 님과의 채팅입니다.",
                    color = Color.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ChatItem() {
    JullTheme {
        Chat()
    }
}