package com.example.okimmo.api

import com.example.okimmo.auth.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        tokenManager.getAccessToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val response = chain.proceed(requestBuilder.build())
        if (response.code == 401) {
            val refreshToken = tokenManager.getRefreshToken() ?: return response
            val newToken = refreshTokenSync(refreshToken)
            if (newToken != null) {
                tokenManager.saveTokens(newToken, refreshToken)
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(newRequest)
            }
        }

        return response
    }

    private fun refreshTokenSync(refreshToken: String): String? = runBlocking {
        return@runBlocking try {
            val client = OkHttpClient.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(AuthApi::class.java)
            val response = api.refreshToken(mapOf("refreshToken" to refreshToken))
            if (response.isSuccessful) response.body()?.token else null
        } catch (e: Exception) {
            null
        }
    }
}