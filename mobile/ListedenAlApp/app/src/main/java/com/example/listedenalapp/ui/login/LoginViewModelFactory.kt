package com.example.listedenalapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.listedenalapp.data.repository.AuthRepository
import com.example.listedenalapp.utils.AuthTokenManager

class LoginViewModelFactory(
    private val authRepository: AuthRepository,
    private val authTokenManager: AuthTokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(authRepository, authTokenManager) as T
    }
}
