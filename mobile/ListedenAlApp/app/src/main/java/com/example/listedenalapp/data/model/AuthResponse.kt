package com.example.listedenalapp.data.model

data class AuthResponse(
    val token: String,
    val message: String,
    val userId: String? = null,
    val username: String? = null
)