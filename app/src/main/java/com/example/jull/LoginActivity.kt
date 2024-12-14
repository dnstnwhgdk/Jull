package com.example.jull

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter

import com.example.jull.ui.theme.JullTheme


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainNavigatorScreen()
        }
    }
}

@Composable
fun MainNavigatorScreen() {
    var itemcho by remember { mutableStateOf(5) }
    val focusManager = LocalFocusManager.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            },
        topBar = {

        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .wrapContentSize()
                ,
                containerColor = Color.Black,
                contentPadding = PaddingValues(horizontal = 40.dp)

            ) {
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .wrapContentSize(),
                    horizontalArrangement = Arrangement.Center, // 아이콘 가운데 정렬
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { itemcho = 1 }) {
                            Icon(Icons.Default.Home, contentDescription = "Home",tint = Color.White)
                        }
                        Text("Home", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { itemcho = 2 }) {
                            Icon(Icons.Default.Menu, contentDescription = "Category",tint = Color.White)
                        }
                        Text("Category", color = Color.White,  fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { itemcho = 3 }) {
                            Icon(Icons.Default.Add, contentDescription = "Sell",tint = Color.White)
                        }
                        Text("Sell", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { itemcho = 4 }) {
                            Icon(Icons.Default.MailOutline, contentDescription = "Message",tint = Color.White)
                        }
                        Text("Message", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { itemcho = 5 }) {
                            Icon(Icons.Default.Person, contentDescription = "MY",tint = Color.White)
                        }
                        Text("MY", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (itemcho) {
                1 -> Home()
                2 -> Category()
                3 -> SellItemPage()
                4 -> Chat()
                5 -> My()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellItemPage() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("내용을 입력해주세요") }
    var price by remember { mutableStateOf("") }

    val brandTypes = listOf(
        BrandType("국내 브랜드", listOf("나이키", "아디다스", "뉴발란스", "푸마", "컨버스")),
        BrandType("수입 브랜드(A~L)", listOf("유니클로", "자라", "H&M", "갭", "탑텐")),
        BrandType("수입 브랜드(M~Z)", listOf("구찌", "샤넬", "루이비통", "프라다", "에르메스")),
        BrandType("이펙터 유형(필수)", listOf(
            "멀티이펙터/모델러/IR로더",
            "오버드라이브/디스토션",
            "퍼즈/부스터",
            "컴프레서/리미터",
            "이퀄라이져/서스테이너",
            "코러스/페이저/플랜저",
            "트레몰로/바이브",
            "로터리/링 모듈레이터",
            "리버브/딜레이/에코",
            "루프/샘플러",
            "하모나이저/피치쉬프터/옥타브",
            "노이즈리덕션/노이즈게이트",
            "볼륨/와우",
            "프리앰프/다이렉트박스",
            "파워서플라이",
            "보코더/토크박스/필터",
            "익스프레션",
            "컨트롤러/풋스위치/채널스위치",
            "그외")),
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("판매 물품 등록", style = MaterialTheme.typography.headlineMedium)

        // Enable scrolling
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 사진 업로드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(Color.Black)
                    ) {
                        Text("사진 업로드", color = Color.White)
                    }
                }
            }

            // 브랜드 유형 리스트
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 16.dp)
            ) {
                for (brandType in brandTypes) {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedItems = remember { mutableStateListOf<String>() }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(brandType.name, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { expanded = !expanded }) {
                                Text("더보기")
                            }
                        }
                        if (expanded) {
                            Column {
                                for (brand in brandType.brands) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedItems.contains(brand),
                                            onCheckedChange = { isChecked ->
                                                if (isChecked) selectedItems.add(brand) else selectedItems.remove(
                                                    brand
                                                )
                                            }
                                        )
                                        Text(brand)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // 제목 입력 텍스트 필드
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목 입력") },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            // 내용 입력 텍스트 필드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(150.dp)
                    .border(1.dp, Color.Gray)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxSize(),

                    )
            }

            // 가격 입력 텍스트 필드
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("가격 입력") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // 등록버튼
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                verticalArrangement = Arrangement.Center, // 수직 방향으로 가운데 정렬
                horizontalAlignment = Alignment.CenterHorizontally // 수평 방향으로 가운데 정렬
            ) {
                Button(
                    onClick = { /* 버튼 클릭 동작 */ },
                    modifier = Modifier
                        .size(width = 200.dp, height = 60.dp),
                    colors = ButtonDefaults.buttonColors(Color.Black)
                ) {
                    Text("이펙터 등록하기", fontSize = 20.sp)
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun My(){
    val tabs = listOf("내 정보", "나의 판매", "찜 목록", "알림")
    var selectedTabIndex by remember { mutableStateOf(0) }

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
            // 샘플 데이터
            var sampleUserInfo = listOf(
                UserInfo("닉네임", "줄쟁이"),
                UserInfo("이름", "정윤재"),
                UserInfo("가입년도", "2019.12.30"),
                UserInfo("전화번호", "010-1234-1234"),
                UserInfo("이메일", "201914**@daejin.ac.kr"),
            )
            when (selectedTabIndex) {
                0 -> ProfileScreen(sampleUserInfo)
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


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    JullTheme {
        MainNavigatorScreen()
    }
}
