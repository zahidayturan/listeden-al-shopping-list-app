package com.example.listedenalapp.data.api

import com.example.listedenalapp.data.model.AuthResponse
import com.example.listedenalapp.data.model.UserLoginRequest
import com.example.listedenalapp.data.model.UserRegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/register") // Kayıt işlemi için POST isteği
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<AuthResponse>

    @POST("api/auth/login") // Giriş işlemi için POST isteği
    suspend fun loginUser(@Body request: UserLoginRequest): Response<AuthResponse>

}