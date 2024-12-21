package com.example.jull

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellItemPage() {
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("내용을 입력해주세요") }
    var price by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val selectedCategories = remember { mutableStateListOf<String>() }
    val storage = FirebaseStorage.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var expanded by remember { mutableStateOf(false) }  // expanded 변수 추가
    var selectedTradeType by remember { mutableStateOf("택배 거래") }


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (imageUris.size < 5) {
                imageUris = imageUris + it
            } else {
                Toast.makeText(context, "최대 5장까지만 업로드 가능합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }


    val brandTypes = listOf(
        BrandType("국내 브랜드", listOf("Altonics", "Amsterdam cream",
            "Artec", "BIGRIG", "Gopherwood", "hushh", "Macron Audio", "Modegear",
            "Moollon", "Musicom", "Nobles", "Ogre", "Radix", "SwitchAudio", "TKI", "TONE BOX")),
        BrandType("수입 브랜드(A~L)", listOf("Anasounds", "BEETRONICS", "BluGuitar", "Bob Burt",
            "Bogner", "Boss", "Catalinbread", "Chase Bliss Audio", "CKK Electronic", "Cool Music",
            "Cornerstone", "Creation Audio Labs", "CTC", "Danelectro", "Diamond pedals", "Digitech (DOD)",
            "Dls", "Dr.J", "DSM & Humboldt", "Dunlop", "E.W.S", "EBS", "Electro Harmonix", "ENGL", "Ernie Ball", "Eventide",
            "Fender", "Flamma", "Fox Pedal", "FreeTheTone", "Fulltone", "GameChanger", "Greenhouse",
            "Greer", "GURUS", "Gus.G", "HEADRUSH", "Horizon Devices", "HOTONE", "Humboldt", "Hungry Robot",
            "Ibanez", "IK Multimedia", "ISP", "J.Rockett Audio", "Jackson Audio", "Jam Pedal", "JHS PEDALS",
            "Joemeek", "JOYO", "Kardian", "Keeley", "Keyztone", "KHDK ELECTRONICS", "Kikutani", "Korg",
            "KSR", "Lehle", "Line6", "Livemaster", "LPD Pedals", "Lucent", "Solar")),
        BrandType("수입 브랜드(M~Z)", listOf("M-Vave", "Mad Professor", "Magnetic Effects",
            "Mastro Valvola", "Matthews", "Maxon", "Mesa/Boogie", "Mission Engineering", "Mod tone",
            "Mooer Audio", "Morley", "Mosky", "MXR", "Neural DSP", "NoahSARK", "Nosepedal", "Nux",
            "Old Blood Noise (OBNE)", "Orange", "Origin Effects", "Paint Audio", "Pedal Pawn",
            "Petty John", "Pigtronix", "Proco", "Providence", "Revv", "Road Rage Progear", "RockBoard",
            "Rocktron", "Roland", "Ross", "Rowin", "Samson", "Shins Music", "Shnobel Tone", "Sinvertek",
            "SKS AUDIO", "Soldano", "Sonicake", "Source Audio", "Strymon", "StudioDaydream", "SUPRO",
            "T1M", "Taurus", "TC Electronic", "Tech21", "TechnTone", "Thermion", "Toms line", "Trial",
            "True Tone (Visual Sound)", "Two Notes", "Umbrella Company", "Valeton", "Vemuram", "Victory",
            "VITAL AUDIO", "Voodoo Lab", "Vox", "Walrus Audio", "Wampler", "Xotic", "Xvive", "Z.Vex", "Zoom")),
        BrandType("이펙터 유형(필수)", listOf("멀티이펙터/모델러/IR로더", "오버드라이브/디스토션", "퍼즈/부스터",
            "컴프레서/리미터", "이퀄라이져/서스테이너", "코러스/페이저/플랜저", "트레몰로/바이브", "로터리/링 모듈레이터",
            "리버브/딜레이/에코", "루프/샘플러", "하모나이저/피치쉬프터/옥타브", "노이즈리덕션/노이즈게이트", "볼륨/와우",
            "프리앰프/다이렉트박스", "파워서플라이", "보코더/토크박스/필터", "익스프레션", "컨트롤러/풋스위치/채널스위치", "그외")),
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
            // 사진 업로드 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 이미지 추가 버튼
                    if (imageUris.size < 5) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "+",
                                    fontSize = 40.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Light
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${imageUris.size}/5",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // 선택된 이미지들 표시
                    imageUris.forEachIndexed { index, uri ->
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected Image $index",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    imageUris = imageUris.filterIndexed { i, _ -> i != index }
                                },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "×",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 거래방식 선택 (Row 밖으로 이동됨)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                OutlinedTextField(
                    value = selectedTradeType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("거래 방식") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("택배 거래", "직거래", "택배거래/직거래").forEach { tradeType ->
                        DropdownMenuItem(
                            text = { Text(tradeType) },
                            onClick = {
                                selectedTradeType = tradeType
                                expanded = false
                            }
                        )
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
                value = if (title.length <= 14) title else title.take(14), // 50자로 제한
                onValueChange = {
                    if (it.length <= 50) title = it
                },
                label = { Text("제목 입력 (최대 14자)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                singleLine = true
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
                        if (imageUris.isEmpty()) {
                            Toast.makeText(context, "최소 1장의 사진을 업로드해주세요", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (title.isEmpty() || price.isEmpty() || selectedCategories.isEmpty()) {
                            Toast.makeText(context, "필수 정보를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        val uploadedUrls = mutableListOf<String>()

                        imageUris.forEachIndexed { index, uri ->
                            val imageRef = storage.reference.child("items/${UUID.randomUUID()}")
                            imageRef.putFile(uri)
                                .continueWithTask { task ->
                                    if (!task.isSuccessful) {
                                        task.exception?.let { throw it }
                                    }
                                    imageRef.downloadUrl
                                }
                                .addOnSuccessListener { downloadUri ->
                                    uploadedUrls.add(downloadUri.toString())

                                    // 모든 이미지가 업로드되면 Firestore에 저장
                                    if (uploadedUrls.size == imageUris.size) {
                                        // 브랜드 카테고리와 이펙터 유형을 분리하여 저장
                                        val brandCategory = selectedCategories.firstOrNull { brand ->
                                            brandTypes[0].brands.contains(brand) ||
                                                    brandTypes[1].brands.contains(brand) ||
                                                    brandTypes[2].brands.contains(brand)
                                        } ?: ""

                                        val effecterType = selectedCategories.firstOrNull { type ->
                                            brandTypes[3].brands.contains(type)
                                        } ?: ""

                                        val item = Item(
                                            sellerId = currentUser.uid,
                                            imageUrl = uploadedUrls.joinToString(","),
                                            title = title,
                                            subtitle = "",
                                            brandCategory = brandCategory,
                                            effecterType = effecterType,
                                            price = price,
                                            description = description,
                                            createdAt = Date(),
                                            tradeType = selectedTradeType
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
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                }
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