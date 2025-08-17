package com.example.listedenalapp.data.api

import com.example.listedenalapp.utils.AuthTokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Volatile
    private var INSTANCE: ApiService? = null

    fun getClient(context: Context): ApiService {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildClient(context).also { INSTANCE = it }
        }
    }

    private fun buildClient(context: Context): ApiService {
        val authTokenManager = AuthTokenManager(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = runCatching {
                kotlinx.coroutines.runBlocking {
                    authTokenManager.getAuthToken()
                }

            }.getOrNull()

            val requestBuilder = originalRequest.newBuilder()

            token?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }

            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}