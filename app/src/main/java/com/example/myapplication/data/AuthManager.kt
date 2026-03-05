package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("awi_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_LOGIN = "user_login"
        private const val KEY_ROLE = "user_role"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUserInfo(login: String, role: String) {
        prefs.edit()
            .putString(KEY_LOGIN, login)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getLogin(): String? = prefs.getString(KEY_LOGIN, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clear() {
        prefs.edit().clear().apply()
    }
}
