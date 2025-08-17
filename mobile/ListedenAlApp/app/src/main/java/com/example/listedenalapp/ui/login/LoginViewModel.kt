package com.example.listedenalapp.ui.login

import NetworkResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listedenalapp.data.model.AuthResponse
import com.example.listedenalapp.data.model.UserLoginRequest
import com.example.listedenalapp.data.repository.AuthRepository
import com.example.listedenalapp.utils.AuthTokenManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<NetworkResult<AuthResponse>>()
    val loginResult: LiveData<NetworkResult<AuthResponse>> = _loginResult

    fun loginUser(email: String, password: String) {
        _loginResult.value = NetworkResult.Loading

        viewModelScope.launch {
            val request = UserLoginRequest(email, password)
            val result = authRepository.loginUser(request)

            _loginResult.value = result

            if (result is NetworkResult.Success) {
                authTokenManager.saveAuthToken(result.data.accessToken)
            }
        }
    }
}