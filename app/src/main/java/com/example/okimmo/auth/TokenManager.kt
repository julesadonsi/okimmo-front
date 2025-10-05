package com.example.okimmo.auth

import android.content.Context
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    fun saveTokens(token: String, refreshToken: String) {
        prefs.edit { putString("token", token).putString("refreshToken", refreshToken) }
    }

    fun getAccessToken(): String? = prefs.getString("token", null)
    fun getRefreshToken(): String? = prefs.getString("refreshToken", null)

    fun clear() {
        prefs.edit { clear() }
    }
}
