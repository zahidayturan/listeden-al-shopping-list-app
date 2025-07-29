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

    // Lazy initialization ile Context'i aldıktan sonra başlatacağız
    @Volatile
    private var INSTANCE: ApiService? = null

    fun getClient(context: Context): ApiService {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildClient(context).also { INSTANCE = it }
        }
    }

    private fun buildClient(context: Context): ApiService {
        val authTokenManager = AuthTokenManager(context)

        // Ağ isteklerini loglamak için Interceptor (debug modunda faydalı)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // İstek ve yanıt body'lerini logla
        }

        // Token'ı her isteğe ekleyecek Interceptor
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = runCatching {
                // Basit bir örnek için, token'ı her seferinde DataStore'dan okuyoruz.
                // Ancak bu .first() çağrısı senkronize bir blokaj yaratır.
                // Daha ileri düzey bir çözüm için RxJava veya daha karmaşık Coroutine yaklaşımları gerekir.
                kotlinx.coroutines.runBlocking { // Geçici olarak Interceptor içinde blocking çağrı yapıyoruz
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
            .addInterceptor(authInterceptor) // Token ekleyiciyi ekle
            .addInterceptor(loggingInterceptor) // Loglayıcıyı ekle (debug için)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // OkHttpClient'i Retrofit'e ata
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}