package com.example.listedenalapp.ui.register

import NetworkResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listedenalapp.data.model.AuthResponse
import com.example.listedenalapp.data.model.UserRegisterRequest
import com.example.listedenalapp.data.repository.AuthRepository
import com.example.listedenalapp.utils.AuthTokenManager
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {

    private val _registerResult = MutableLiveData<NetworkResult<AuthResponse>>()
    val registerResult: LiveData<NetworkResult<AuthResponse>> = _registerResult

    fun registerUser(userRegisterRequest: UserRegisterRequest) {
        _registerResult.value = NetworkResult.Loading

        viewModelScope.launch {
            val result = authRepository.registerUser(userRegisterRequest)

            _registerResult.value = result

            if (result is NetworkResult.Success) {
                authTokenManager.saveAuthToken(result.data.accessToken)
            }
        }
    }
}
