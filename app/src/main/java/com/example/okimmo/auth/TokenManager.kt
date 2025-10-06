package com.example.okimmo.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.okimmo.model.UserResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder().setLenient().create()

    companion object {
        private const val KEY_ACCESS_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refreshToken"
        private const val KEY_USER_INFOS = "user_infos"
    }

    fun saveTokens(token: String, refreshToken: String) {
        prefs.edit { putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken) }
    }
    fun saveUserInfos(userInfos: UserResponse) {
        val json = gson.toJson(userInfos)
        prefs.edit { putString(KEY_USER_INFOS, json) }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUserInfos(): UserResponse? {
        val json = prefs.getString(KEY_USER_INFOS, null) ?: return null
        return try {
            gson.fromJson(json, UserResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    fun clear() {
        prefs.edit { clear() }
    }
}
