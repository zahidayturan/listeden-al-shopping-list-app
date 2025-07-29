package com.example.listedenalapp.data.model

data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)