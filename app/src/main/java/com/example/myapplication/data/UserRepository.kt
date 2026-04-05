package com.example.myapplication.data

import com.example.myapplication.model.CreateUserInput
import com.example.myapplication.model.UpdateUserRoleInput
import com.example.myapplication.model.User
import com.example.myapplication.network.ApiService

class UserRepository(private val api: ApiService) {

    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = api.getUsers()
            if (response.isSuccessful) {
                Result.Success(response.body() ?: emptyList())
            } else {
                Result.Error(response.toApiErrorMessage("Erreur lors du chargement des utilisateurs"))
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun createUser(user: CreateUserInput): Result<User> {
        return try {
            val response = api.createUser(user)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Erreur de décodage lors de la création de l'utilisateur")
            } else {
                Result.Error(response.toApiErrorMessage("Erreur lors de la création de l'utilisateur"))
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun updateUserRole(id: Int, payload: UpdateUserRoleInput): Result<User> {
        return try {
            val response = api.updateUserRole(id, payload)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Erreur de décodage lors de la mise à jour du rôle")
            } else {
                Result.Error(response.toApiErrorMessage("Erreur lors de la mise à jour du rôle"))
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun deleteUser(id: Int): Result<Unit> {
        return try {
            val response = api.deleteUser(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(response.toApiErrorMessage("Erreur lors de la suppression de l'utilisateur"))
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }
}
