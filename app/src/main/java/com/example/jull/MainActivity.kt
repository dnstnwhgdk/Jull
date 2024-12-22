package com.example.jull

import SignUpScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jull.ui.theme.JullTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import com.example.jull.RecoveryType
import com.google.firebase.database.FirebaseDatabase


class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JullTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                    //MainLogo(modifier = Modifier.padding(innerPadding))
                }
            }
        }

    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignUpScreen(navController)
        }
    }
}
@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf<RecoveryType?>(null) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 로고와 타이틀 부분
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Image(
                painter = painterResource(id = R.drawable.eflog),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )

            Text(
                text = "멜로디 마트",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "당신의 음악을 더 풍성하게",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Text(
                text = "이펙터 거래의 새로운 기준",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 이메일 및 비밀번호 입력 필드
        Column {
            Text(
                text = "아이디",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("아이디 입력") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "비밀번호",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
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
                textStyle = TextStyle(fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }

        // 로그인 버튼 및 하단 링크
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(context, LoginActivity::class.java)
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("로그인", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { navController.navigate("signup") }) {
                    Text("회원 가입")
                }
                TextButton(onClick = { showRecoveryDialog = RecoveryType.ID }) {
                    Text("아이디 찾기")
                }
                TextButton(onClick = { showRecoveryDialog = RecoveryType.PASSWORD }) {
                    Text("비밀번호 찾기")
                }
            }
        }
    }

    // 아이디/비밀번호 찾기 다이얼로그
    showRecoveryDialog?.let { type ->
        RecoveryDialog(
            type = type,
            onDismiss = { showRecoveryDialog = null },
            onSubmit = { name, input ->
                when (type) {
                    RecoveryType.ID -> {
                        val database = FirebaseDatabase.getInstance()
                        database.reference.child("users")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                var userEmail: String? = null
                                snapshot.children.forEach { childSnapshot ->
                                    val childName = childSnapshot.child("name").getValue(String::class.java)
                                    val childPhonenum = childSnapshot.child("phonenum").getValue(String::class.java)
                                    if (childName == name && childPhonenum == input) {
                                        userEmail = childSnapshot.child("email").getValue(String::class.java)
                                        return@forEach
                                    }
                                }

                                if (userEmail != null) {
                                    Toast.makeText(
                                        context,
                                        "찾은 아이디: $userEmail",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "입력하신 정보와 일치하는 계정이 없습니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }

                    RecoveryType.PASSWORD -> {
                        if (input.contains("@")) {
                            auth.sendPasswordResetEmail(input)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "비밀번호 재설정 이메일을 발송했습니다",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "등록되지 않은 이메일 주소입니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
                showRecoveryDialog = null
            }
        )
    }
}

@Composable
fun RecoveryDialog(
    type: RecoveryType,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(when (type) {
                RecoveryType.ID -> "이름과 휴대폰 번호로\n아이디 찾기"
                RecoveryType.PASSWORD -> "비밀번호 재설정"
            })
        },
        text = {
            Column {
                Text(when (type) {
                    RecoveryType.ID -> "가입 시 등록한 이름과 휴대폰 번호를\n입력해주세요"
                    RecoveryType.PASSWORD -> "가입한 이메일 주소를 입력해주세요"
                })
                Spacer(modifier = Modifier.height(8.dp))

                if (type == RecoveryType.ID) {
                    // 이름 입력 필드
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            isError = false
                        },
                        placeholder = { Text("이름 입력") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isError,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        isError = false
                        if (type == RecoveryType.ID) {
                            input = it.filter { char -> char.isDigit() }
                            if (input.length > 11) input = input.take(11)
                        }
                    },
                    placeholder = {
                        Text(when (type) {
                            RecoveryType.ID -> "휴대폰 번호 입력 ('-' 제외)"
                            RecoveryType.PASSWORD -> "이메일 주소 입력"
                        })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = when (type) {
                            RecoveryType.ID -> KeyboardType.Number
                            RecoveryType.PASSWORD -> KeyboardType.Email
                        }
                    )
                )
                if (isError) {
                    Text(
                        text = when (type) {
                            RecoveryType.ID -> "이름과 휴대폰 번호를 모두 올바르게 입력해주세요"
                            RecoveryType.PASSWORD -> "올바른 이메일 주소를 입력해주세요"
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (type) {
                        RecoveryType.ID -> {
                            if (name.isNotBlank() && input.length == 11) {
                                onSubmit(name, input)
                            } else {
                                isError = true
                            }
                        }
                        RecoveryType.PASSWORD -> {
                            if (input.contains("@")) {
                                onSubmit("", input)
                            } else {
                                isError = true
                            }
                        }
                    }
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JullTheme {
        Greeting()
    }
}
