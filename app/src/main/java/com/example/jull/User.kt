package com.example.jull

import java.time.LocalDate

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val nickname: String = "",
    val phonenum: String = "",
    val date: LocalDate = LocalDate.now()
)