package com.example.myapplication.data

import com.example.myapplication.model.GameCreateInput
import com.example.myapplication.model.GameInput
import com.example.myapplication.model.GameWithEditor
import com.example.myapplication.network.ApiService

class GameRepository(private val api: ApiService) {

    suspend fun getGames(editorId: Int? = null): Result<List<GameWithEditor>> {
        return try {
            val response = api.getGames(editorId)
            if (response.isSuccessful) {
                Result.Success(response.body() ?: emptyList())
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun getGameById(id: Int): Result<GameWithEditor> {
        return try {
            val response = api.getGameById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Jeu introuvable")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun createGame(game: GameCreateInput): Result<GameWithEditor> {
        return try {
            val response = api.createGame(game)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Erreur lors de la création du jeu")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun updateGame(id: Int, game: GameInput): Result<GameWithEditor> {
        return try {
            val response = api.updateGame(id, game)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Erreur lors de la modification du jeu")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun deleteGame(id: Int): Result<Unit> {
        return try {
            val response = api.deleteGame(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }
}
