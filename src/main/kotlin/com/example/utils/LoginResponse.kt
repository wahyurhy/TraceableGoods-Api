package com.example.utils

data class LoginResponse<out T>(val code: Int, val message: String, val data: T)