package com.example.jull

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun Home(){
    var searchText by remember { mutableStateOf("") }
    var selectedButtonIndex by remember { mutableStateOf(0) }
    val items = listOf(
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품1", "부제목1", "카테고리1", "10,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/3431/650_87462.jpg", "상품2", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/1594/650_62488.jpg", "상품3", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/3432/650_56745.jpg", "상품4", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/369/650_83618.jpg", "상품5", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/3431/650_54937.jpg", "상품6", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/3381/quadcortex2_650.jpg", "상품7", "부제목2", "카테고리2", "20,000원"),
        Item("https://img.schoolmusic.co.kr/prod_picture/22/1815/650_57193.jpg", "상품8", "부제목2", "카테고리2", "20,000원"),
    )
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically // 텍스트 필드와 아이콘을 수직으로 정렬
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("브랜드,상품,종류 등") },
                    modifier = Modifier.weight(1f) // 텍스트 필드가 남은 공간을 채우도록 설정
                )
                IconButton(onClick = { /* 알람 아이콘 클릭 동작 */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = "알람")
                }
            }
            LazyRow {
                items(6) { index ->
                    TextButton(
                        onClick = { selectedButtonIndex = index },
                        colors = ButtonDefaults.textButtonColors( // 텍스트 색상 변경
                            contentColor = if (selectedButtonIndex == index) Color.Black else Color.Gray
                        ),
                    ) {
                        when (index) {
                            0 -> Text("인기상품")
                            1 -> Text("최신상품")
                            2 -> Text("빈티지")
                            3 -> Text("저가상품")
                            4 -> Text("고가상품")
                            5 -> Text("베스트상품")
                            6 -> Text("베스트상품")
                        }
                    }
                }
            }
        }
        ItemBord(items)
    }
}