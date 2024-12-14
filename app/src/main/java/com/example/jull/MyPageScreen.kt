package com.example.jull

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun My() {
    val tabs = listOf("내 정보", "나의 판매", "찜 목록", "알림")
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

                        // 가입년도 정보 가져오기
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
                UserInfo("이메일", "로그인 필요"),
            )
        }
    }

    // Scaffold UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("마이페이지") },
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content based on selected tab
            when (selectedTabIndex) {
                0 -> ProfileScreen(userInfoList)
                1 -> MyItem()
                2 -> Wishlist()
                3 -> Text("알림/블라인드 화면", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
@Composable
fun MyItem(){
    var searchText by remember { mutableStateOf("") }
    var selectedButtonIndex by remember { mutableStateOf(0) }
    val items = listOf(
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품1", "부제목1", "카테고리1", "10,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품2", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품3", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품4", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품5", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품6", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품7", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품8", "부제목2", "카테고리2", "20,000원"),
    )
    ItemBord(items)
}


@Composable
fun Wishlist(){
    var searchText by remember { mutableStateOf("") }
    var selectedButtonIndex by remember { mutableStateOf(0) }
    val items = listOf(
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품1", "부제목1", "카테고리1", "10,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품2", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품3", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품4", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품5", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품6", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품7", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품8", "부제목2", "카테고리2", "20,000원"),
    )
    ItemBord(items)
}

