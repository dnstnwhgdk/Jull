package com.example.jull

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun ChatNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "itemDetail") {
        composable("itemDetail") { backStackEntry ->
            ItemDetailScreen(
                imageUrl = "",
                title = "",
                price = "",
                brandCategory = "",
                effecterType = "",
                description = "",
                sellerId = "sellerId1",
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable("chat/{chatRoomId}") { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
            ChatScreen(chatRoomId)
        }
    }
}
