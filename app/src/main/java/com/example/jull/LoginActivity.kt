package com.example.jull


import ChatRoomsScreen
import ChatScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jull.ui.theme.JullTheme
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : ComponentActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainNavigatorScreen()
        }
        firebaseAuth = FirebaseAuth.getInstance()
    }
}

@Composable
fun MainNavigatorScreen() {
    val navController = rememberNavController()
    val onBackPressed: () -> Unit = {}

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { Home() }
            composable("board") { Board() }  // 게시판 화면으로 변경
            composable("sell") { SellItemPage() }
            composable("message") {
                ChatRoomsScreen(
                    onChatRoomClick = { chatRoomId ->
                        navController.navigate("chat/$chatRoomId")
                    }
                )
            }
            composable("my") { My() }
            composable("chat/{chatRoomId}") { backStackEntry ->
                val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
                ChatScreen(chatRoomId, onBackPressed)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("board", "게시판", Icons.Default.Menu),
        BottomNavItem("sell", "Sell", Icons.Default.Add),
        BottomNavItem("message", "Message", Icons.Default.MailOutline),
        BottomNavItem("my", "MY", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color.Black,
        contentColor = Color.White
    ) {
        val currentDestination = navController.currentBackStackEntryAsState()?.value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // 중복된 화면 스택 방지를 위해 설정
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(item.icon, contentDescription = item.label)
                },
                label = {
                    Text(text = item.label, fontSize = 12.sp, color = Color.White)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.DarkGray
                )
            )
        }
    }
}
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    JullTheme {
        MainNavigatorScreen()
    }
}
