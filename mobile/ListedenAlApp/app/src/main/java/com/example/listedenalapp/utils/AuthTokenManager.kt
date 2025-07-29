package com.example.listedenalapp.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore'u bir kere tanımlamak için
// Uygulamanızın Context'ini kullanarak erişim sağlayacağız
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthTokenManager(private val context: Context) {

    // Token için DataStore anahtarı
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

    /**
     * Kimlik doğrulama tokenını DataStore'a kaydeder.
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    /**
     * DataStore'dan kimlik doğrulama tokenını okur.
     * Token yoksa null döner.
     */
    suspend fun getAuthToken(): String? {
        return context.dataStore.data
            .map { preferences ->
                preferences[AUTH_TOKEN_KEY]
            }.first() // Akıştaki ilk değeri alır ve akışı kapatır
    }

    /**
     * DataStore'dan kimlik doğrulama tokenını siler (çıkış yaparken kullanılır).
     */
    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }
}