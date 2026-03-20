package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("awi_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGIN = "user_login"
        private const val KEY_ROLE = "user_role"
        private const val KEY_SESSION_ACTIVE = "session_active"
    }

    fun saveUserInfo(login: String, role: String) {
        prefs.edit()
            .putString(KEY_LOGIN, login)
            .putString(KEY_ROLE, role)
            .putBoolean(KEY_SESSION_ACTIVE, true)
            .apply()
    }

    fun getLogin(): String? = prefs.getString(KEY_LOGIN, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_SESSION_ACTIVE, false)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
