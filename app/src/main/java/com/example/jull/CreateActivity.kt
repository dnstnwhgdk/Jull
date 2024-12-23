import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate


@Composable
fun SignUpScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordcheck by remember { mutableStateOf("") }
    var usernick by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phonenum by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var agreeAll by remember { mutableStateOf(false) }
    val agreeItems = listOf(
        "만 14세 이상입니다",
        "이용약관 동의",
        "개인 정보 수집 및 이용 동의",
        "개인 정보 수집 및 이용 동의 (선택)",
        "광고성 정보 수신 모두 동의 (선택)"
    )
    val agreementStates = remember { mutableStateListOf(*Array(agreeItems.size) { false }) }
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()).pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            },

    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                navController.navigate("login")
            }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "뒤로가기"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "회원가입",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 이메일 입력
        Text(
            text = "이메일 주소 *",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            isError = !email.contains("@"), // 간단한 이메일 유효성 검증
            placeholder = { Text("올바른 이메일을 입력해주세요.") },
            modifier = Modifier.fillMaxWidth()
        )
        if (!email.contains("@")) {
            Text(
                text = "올바른 이메일을 입력해주세요.",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 입력
        Text(
            text = "비밀번호 *",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("비밀번호 입력") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password Visibility"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // 비밀번호 확인
        Text(
            text = "비밀번호 확인",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = passwordcheck,
            onValueChange = { passwordcheck = it },
            placeholder = { Text("비밀번호 확인") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        if (passwordcheck != password) {
            Text(
                text = "비밀번호를 확인해주세요",
                color = Color.Red,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "닉네임",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = usernick,
            onValueChange = { usernick = it },
            placeholder = { Text("닉네임을 입력하세요") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "이름",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("이름을 입력하세요") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        // 핸드폰 번호 입력
        Text(
            text = "핸드폰 번호",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = phonenum,
            onValueChange = { phonenum = it },
            placeholder = { Text("핸드폰 번호를 입력하세요") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 약관 동의
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = agreeAll,
                onCheckedChange = {
                    agreeAll = it
                    agreementStates.replaceAll { _ -> it }
                }
            )
            Text(
                text = "모두 동의합니다",
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "선택 동의 항목 포함",
            color = Color.Gray,
            fontSize = 12.sp
        )

        agreementStates.forEachIndexed { index, isChecked ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        agreementStates[index] = checked
                        agreeAll = agreementStates.all { it }
                    }
                )
                Text(
                    text = "[${if (index < 3) "필수" else "선택"}] ${agreeItems[index]}",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { }) {
                    Text("내용 보기", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 회원가입 버튼
        Button(
            onClick = {
                // Firebase Authentication 회원가입
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                // Firebase Realtime Database에 사용자 정보 저장
                                val userId = user.uid
                                val userInfo = com.example.jull.User(
                                    uid = userId,
                                    email = email,
                                    name = username,
                                    nickname = usernick,
                                    phonenum = phonenum,
                                    date = LocalDate.now()
                                )
                                database.child("users").child(userId).setValue(userInfo)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "회원가입 성공!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Log.e("SignUp", "DB 저장 실패: ${dbTask.exception}")
                                        }
                                    }
                            }
                        } else {
                            Log.e("SignUp", "회원가입 실패: ${task.exception}")
                        }
                    }
            },
            enabled = agreementStates[0] && agreementStates[1] && agreementStates[2], // 필수 항목 체크 여부
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (agreementStates[0] && agreementStates[1] && agreementStates[2]) Color.Black else Color.Gray)
        ) {
            Text(
                text = "가입하기",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

