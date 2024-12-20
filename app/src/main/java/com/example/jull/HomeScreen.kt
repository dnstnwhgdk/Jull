package com.example.jull

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var selectedButtonIndex by remember { mutableStateOf(0) }
    var originalItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var filteredItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var itemFavoriteCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    val focusManager = LocalFocusManager.current

    val brandTypes = listOf(
        BrandType("국내 브랜드", listOf("Altonics", "Amsterdam cream",
            "Artec", "BIGRIG", "Gopherwood", "hushh", "Macron Audio", "Modegear",
            "Moollon", "Musicom", "Nobles", "Ogre", "Radix", "SwitchAudio", "TKI", "TONE BOX")),
        BrandType("수입 브랜드(A~L)", listOf("Anasounds", "BEETRONICS", "BluGuitar", "Bob Burt",
            "Bogner", "Boss", "Catalinbread", "Chase Bliss Audio", "CKK Electronic", "Cool Music",
            "Cornerstone", "Creation Audio Labs", "CTC", "Danelectro", "Diamond pedals", "Digitech (DOD)",
            "Dls", "Dr.J", "DSM & Humboldt", "Dunlop", "E.W.S", "EBS", "Electro Harmonix", "ENGL",
            "Ernie Ball", "Eventide", "Fender", "Flamma", "Fox Pedal", "FreeTheTone", "Fulltone",
            "GameChanger", "Greenhouse", "Greer", "GURUS", "Gus.G", "HEADRUSH", "Horizon Devices",
            "HOTONE", "Humboldt", "Hungry Robot", "Ibanez", "IK Multimedia", "ISP", "J.Rockett Audio",
            "Jackson Audio", "Jam Pedal", "JHS PEDALS", "Joemeek", "JOYO", "Kardian", "Keeley",
            "Keyztone", "KHDK ELECTRONICS", "Kikutani", "Korg", "KSR", "Lehle", "Line6", "Livemaster",
            "LPD Pedals", "Lucent", "Solar")),
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
            "프리앰프/다이렉트박스", "파워서플라이", "보코더/토크박스/필터", "익스프레션", "컨트롤러/풋스위치/채널스위치", "그외"))
    )

    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("favorites")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val counts = snapshot.documents
                        .groupBy { it.getString("itemId") ?: "" }
                        .mapValues { it.value.size }
                    itemFavoriteCounts = counts
                }
            }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = "데이터 로드 실패: ${e.message}"
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    originalItems = snapshot.documents.mapNotNull { doc ->
                        try {
                            Item.fromMap(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            null
                        }
                    }
                    isLoading = false
                }
            }
    }

    LaunchedEffect(searchText, selectedCategories, selectedButtonIndex, originalItems) {
        val searchFiltered = if (searchText.isEmpty()) originalItems
        else originalItems.filter { item ->
            item.title.contains(searchText, ignoreCase = true) ||
                    item.brandCategory.contains(searchText, ignoreCase = true) ||
                    item.effecterType.contains(searchText, ignoreCase = true)
        }

        val categoryFiltered = if (selectedCategories.isEmpty()) searchFiltered
        else searchFiltered.filter { item ->
            selectedCategories.all { category ->
                item.brandCategory == category || item.effecterType == category
            }
        }

        filteredItems = when (selectedButtonIndex) {
            0 -> categoryFiltered.sortedByDescending { item -> itemFavoriteCounts[item.id] ?: 0 }
            1 -> categoryFiltered
            2 -> categoryFiltered.sortedBy { it.price.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0 }
            3 -> categoryFiltered.sortedByDescending { it.price.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0 }
            else -> categoryFiltered
        }
    }

    Column(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { focusManager.clearFocus() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("브랜드,상품,종류 등") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { showCategorySheet = true }) {
                            Icon(Icons.Default.Add, contentDescription = "카테고리 선택")
                        }
                    }
                )
                IconButton(onClick = { /* 추후 기능 추가 예정 */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = "알림")
                }
            }

            if (selectedCategories.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(vertical = 0.dp)
                ) {
                    selectedCategories.forEach { category ->
                        AssistChip(
                            onClick = { },
                            label = { Text(category) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        selectedCategories = selectedCategories - category
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "제거",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            },
                            modifier = Modifier.padding(end = 2.dp, bottom = 2.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("인기상품", "최신상품", "저가상품", "고가상품").forEachIndexed { index, text ->
                    TextButton(
                        onClick = { selectedButtonIndex = index },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (selectedButtonIndex == index) Color.Black else Color.Gray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text)
                    }
                }
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage ?: "오류가 발생했습니다")
                }
            }
            filteredItems.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("등록된 상품이 없습니다")
                }
            }
            else -> {
                ItemBord(
                    items = filteredItems,
                    onItemClick = { item ->
                        val intent = Intent(context, ItemDetailActivity::class.java).apply {
                            putExtra("imageUrl", item.imageUrl)
                            putExtra("title", item.title)
                            putExtra("price", item.price)
                            putExtra("brandCategory", item.brandCategory)
                            putExtra("effecterType", item.effecterType)
                            putExtra("description", item.description)
                            putExtra("sellerId", item.sellerId)
                            putExtra("id", item.id)
                            putExtra("createdAt", item.createdAt)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    "카테고리 선택",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                brandTypes.forEach { brandType ->
                    var expanded by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                brandType.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.weight(1f))
                            Icon(
                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "접기" else "펼치기"
                            )
                        }

                        if (expanded) {
                            FlowRow {
                                brandType.brands.forEach { brand ->
                                    FilterChip(
                                        selected = selectedCategories.contains(brand),
                                        onClick = {
                                            selectedCategories = if (selectedCategories.contains(brand)) {
                                                selectedCategories - brand
                                            } else {
                                                selectedCategories + brand
                                            }
                                        },
                                        label = { Text(brand) },
                                        modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (brandType != brandTypes.last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}