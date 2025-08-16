package com.example.listedenalapp.data.repository

import com.example.listedenalapp.data.api.ApiService
import com.example.listedenalapp.data.model.AuthResponse
import com.example.listedenalapp.data.model.UserLoginRequest
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    suspend fun loginUser(request: UserLoginRequest): Response<AuthResponse> {
        return apiService.loginUser(request)
    }
}
