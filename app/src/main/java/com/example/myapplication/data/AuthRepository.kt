package com.example.myapplication.data

import com.example.myapplication.model.LoginRequest
import com.example.myapplication.network.ApiService

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class AuthRepository(
    private val api: ApiService,
    private val authManager: AuthManager
) {

    suspend fun login(login: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequest(login, password))
            if (response.isSuccessful) {
                val body = response.body()
                // On stocke le token depuis le header Authorization de la réponse
                // ou depuis le champ accessToken si le backend le retourne
                val token = response.headers()["Authorization"]?.removePrefix("Bearer ")
                    ?: body?.accessToken

                if (token != null) {
                    authManager.saveToken(token)
                }
                val userInfo = body?.user
                if (userInfo != null) {
                    authManager.saveUserInfo(userInfo.login, userInfo.role)
                }
                Result.Success(Unit)
            } else {
                Result.Error("Login ou mot de passe incorrect")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            api.logout()
            authManager.clear()
            Result.Success(Unit)
        } catch (e: Exception) {
            authManager.clear()
            Result.Success(Unit)
        }
    }

    suspend fun whoami(): Result<Pair<String, String>> {
        return try {
            val response = api.whoami()
            if (response.isSuccessful) {
                val user = response.body()?.get("user")
                if (user != null) {
                    Result.Success(Pair(user.login, user.role))
                } else {
                    Result.Error("Utilisateur introuvable")
                }
            } else {
                Result.Error("Non authentifié")
            }
        } catch (e: Exception) {
            Result.Error("Erreur réseau : ${e.localizedMessage}")
        }
    }
}
