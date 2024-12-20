package com.example.jull

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

data class UserInfo(val label: String, val value: String) {
    constructor() : this("", "")
}

@Composable
fun ProfileScreen(userInfoList: List<UserInfo>) {
    var isPasswordDialogOpen by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
        ) {
            Image(
                painter = painterResource(id = R.drawable.eflog),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(150.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Button(
            onClick = { isPasswordDialogOpen = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("개인정보 수정", color = Color.White, fontSize = 16.sp)
        }

        if (isPasswordDialogOpen) {
            PasswordPopupDialog(
                onClose = { isPasswordDialogOpen = false },
                onPasswordConfirmed = {
                    isPasswordDialogOpen = false
                    showEditDialog = true
                }
            )
        }

        if (showEditDialog) {
            EditProfileDialog(
                currentUserInfo = userInfoList,
                onClose = { showEditDialog = false }
            )
        }
    }
}

@Composable
fun PasswordPopupDialog(
    onClose: () -> Unit,
    onPasswordConfirmed: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("비밀번호 확인", fontSize = 20.sp) },
        text = {
            Column {
                Text("개인정보를 수정하려면 비밀번호를 입력하세요.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("비밀번호") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentUser != null) {
                        val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
                        currentUser.reauthenticate(credential)
                            .addOnSuccessListener {
                                onPasswordConfirmed()
                                Toast.makeText(context, "비밀번호가 확인되었습니다", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("취소")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun EditProfileDialog(
    currentUserInfo: List<UserInfo>,
    onClose: () -> Unit
) {
    var nickname by remember { mutableStateOf(currentUserInfo.find { it.label == "닉네임" }?.value ?: "") }
    var name by remember { mutableStateOf(currentUserInfo.find { it.label == "이름" }?.value ?: "") }
    var phonenum by remember { mutableStateOf(currentUserInfo.find { it.label == "전화번호" }?.value ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("개인정보 수정", fontSize = 20.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("닉네임") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("이름") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phonenum,
                    onValueChange = { phonenum = it },
                    label = { Text("전화번호") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("새 비밀번호 (변경시에만 입력)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("새 비밀번호 확인") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentUser != null) {
                        if (newPassword.isNotEmpty()) {
                            if (newPassword != confirmPassword) {
                                Toast.makeText(context, "새 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            currentUser.updatePassword(newPassword)
                                .addOnFailureListener {
                                    Toast.makeText(context, "비밀번호 변경 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }

                        val database = FirebaseDatabase.getInstance()
                        val userRef = database.reference.child("users").child(currentUser.uid)

                        val updates = hashMapOf<String, Any>(
                            "nickname" to nickname,
                            "name" to name,
                            "phonenum" to phonenum
                        )

                        userRef.updateChildren(updates)
                            .addOnSuccessListener {
                                Toast.makeText(context, "개인정보가 수정되었습니다", Toast.LENGTH_SHORT).show()
                                onClose()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "개인정보 수정 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("수정")
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("취소")
            }
        }
    )
}