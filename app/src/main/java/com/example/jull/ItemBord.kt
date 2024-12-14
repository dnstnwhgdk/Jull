package com.example.jull

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ItemBord(
    items: List<Item>,
    onItemClick: (Item) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(items.chunked(1)) { rowItems ->
            Row {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { onItemClick(item) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = "상품 이미지",
                                )
                                Text(item.title, fontWeight = FontWeight.Bold)
                                Text(item.subtitle)
                                Text(item.category)
                                Text(item.price, fontWeight = FontWeight.Bold)
                            }
                            // 하트 아이콘 (오른쪽 상단)
                            var isFavorite by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { isFavorite = !isFavorite },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Favorite" else "Not Favorite",
                                    tint = if (isFavorite) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ItemBordPreview() {
    val items = listOf(
        Item("https://img.schoolmusic.co.kr/prod_picture/22/13/650_23171.jpg", "상품1", "부제목1", "카테고리1", "10,000원"),
        Item("https://example.com/image2.jpg", "상품2", "부제목2", "카테고리2", "20,000원"),
        Item("https://example.com/image2.jpg", "상품3", "부제목2", "카테고리2", "20,000원"),
        Item("https://example.com/image2.jpg", "상품4", "부제목2", "카테고리2", "20,000원"),
        Item("https://example.com/image2.jpg", "상품5", "부제목2", "카테고리2", "20,000원"),
        Item("https://example.com/image2.jpg", "상품6", "부제목2", "카테고리2", "20,000원"),
        Item("https://example.com/image2.jpg", "상품7", "부제목2", "카테고리2", "20,000원"),
        Item("https://example.com/image2.jpg", "상품8", "부제목2", "카테고리2", "20,000원"),
    )

    ItemBord(items = items)
}