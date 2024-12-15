package com.example.jull

enum class RecoveryType {
    ID,
    PASSWORD;

    fun getTitle(): String {
        return when (this) {
            ID -> "아이디 찾기"
            PASSWORD -> "비밀번호 찾기"
        }
    }
}