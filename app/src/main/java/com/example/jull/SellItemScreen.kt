package com.example.jull

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellItemPage() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("내용을 입력해주세요") }
    var price by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val selectedCategories = remember { mutableStateListOf<String>() }
    val storage = FirebaseStorage.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

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

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("판매 물품 등록", style = MaterialTheme.typography.headlineMedium)

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
                                            checked = selectedCategories.contains(brand),
                                            onCheckedChange = { isChecked ->
                                                if (isChecked) selectedCategories.add(brand)
                                                else selectedCategories.remove(brand)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // 등록버튼
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        val currentUser = auth.currentUser
                        if (currentUser == null) {
                            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (imageUri == null || title.isEmpty() || price.isEmpty() || selectedCategories.isEmpty()) {
                            Toast.makeText(context, "필수 정보를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true

                        // 이미지 업로드
                        val imageRef = storage.reference.child("items/${UUID.randomUUID()}")
                        imageRef.putFile(imageUri!!)
                            .continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let { throw it }
                                }
                                imageRef.downloadUrl
                            }
                            .addOnSuccessListener { downloadUri ->
                                // Firestore에 상품 정보 저장
                                val item = Item(
                                    sellerId = currentUser.uid,
                                    imageUrl = downloadUri.toString(),
                                    title = title,
                                    subtitle = "",
                                    category = selectedCategories.joinToString(", "),
                                    price = price,
                                    description = description,
                                    createdAt = Date()
                                )

                                firestore.collection("items")
                                    .add(item.toMap())
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "상품이 등록되었습니다", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                isLoading = false
                            }
                    },
                    modifier = Modifier.size(width = 200.dp, height = 60.dp),
                    colors = ButtonDefaults.buttonColors(Color.Black)
                ) {
                    Text("이펙터 등록하기", fontSize = 20.sp)
                }
            }
        }
    }
}