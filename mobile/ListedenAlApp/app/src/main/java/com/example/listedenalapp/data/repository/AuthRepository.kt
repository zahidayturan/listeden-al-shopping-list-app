package com.example.listedenalapp.data.repository

import NetworkResult
import com.example.listedenalapp.data.api.ApiService
import com.example.listedenalapp.data.model.AuthResponse
import com.example.listedenalapp.data.model.UserLoginRequest
import retrofit2.HttpException
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    private suspend fun <T : Any> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("Boş yanıt geldi", response.code())
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Bilinmeyen hata"
                NetworkResult.Error(errorMessage, response.code())
            }
        } catch (e: HttpException) {
            NetworkResult.Error("HTTP Hatası: ${e.message()}", e.code())
        } catch (e: Exception) {
            NetworkResult.Error("Bağlantı hatası: ${e.message}")
        }
    }

    suspend fun loginUser(request: UserLoginRequest): NetworkResult<AuthResponse> {
        return safeApiCall { apiService.loginUser(request) }
    }
}