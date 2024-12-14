package com.example.jull

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// 데이터 클래스 정의
data class UserInfo(val label: String, val value: String){
    constructor() : this("", "")
}

@Composable
fun ProfileScreen(userInfoList: List<UserInfo>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.Black, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // 이미지 리소스
                contentDescription = "Profile Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 사용자 정보 출력
        userInfoList.forEach { info ->
            Text(
                text = "${info.label}: ${info.value}",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = if (info.label == "이름") FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 수정 버튼
        Button(
            onClick = { /* 버튼 클릭 동작 */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("개인정보 수정", color = Color.White, fontSize = 16.sp)
        }
    }
}



// 미리보기
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(userInfoList = sampleUserInfo)
}
val sampleUserInfo = listOf(
    UserInfo("닉네임", "줄쟁이"),
    UserInfo("이름", "정윤재"),
    UserInfo("가입년도", "2019.12.30"),
    UserInfo("전화번호", "010-1234-1234"),
    UserInfo("이메일", "201914**@daejin.ac.kr"),
)
