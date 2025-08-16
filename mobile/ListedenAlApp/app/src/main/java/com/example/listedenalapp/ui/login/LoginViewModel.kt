package com.example.listedenalapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listedenalapp.data.model.UserLoginRequest
import com.example.listedenalapp.data.repository.AuthRepository
import com.example.listedenalapp.utils.AuthTokenManager
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository, private val authTokenManager: AuthTokenManager) : ViewModel() {


    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loginUser(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = UserLoginRequest(email, password)
                val response = authRepository.loginUser(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        authTokenManager.saveAuthToken(it.accessToken)
                        onSuccess("Hoş geldiniz!")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Geçersiz kimlik bilgileri"
                        else -> "Bilinmeyen hata"
                    }
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                onError("Bağlantı hatası")
            } finally {
                _isLoading.value = false
            }
        }
    }
}