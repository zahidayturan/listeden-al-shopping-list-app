package com.example.listedenalapp.data.model

data class AuthResponse(
    val accessToken: String,
    val email: String? = null
)