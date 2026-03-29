package com.example.myapplication.data

import com.example.myapplication.model.Festival
import com.example.myapplication.model.Jeu
import com.example.myapplication.network.ApiService

class FestivalRepository(private val api: ApiService) {

    suspend fun getFestivals(): Result<List<Festival>> {
        return try {
            val response = api.getFestivals()
            if (response.isSuccessful) {
                Result.Success(response.body() ?: emptyList())
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun getFestival(id: Int): Result<Festival> {
        return try {
            val response = api.getFestival(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Festival introuvable")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun getFestivalGames(id: Int): Result<List<Jeu>> {
        return try {
            val response = api.getFestivalGames(id)
            if (response.isSuccessful) {
                Result.Success(response.body() ?: emptyList())
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun createFestival(request: com.example.myapplication.model.CreateFestivalRequest): Result<Festival> {
        return try {
            val response = api.createFestival(request)
            if (response.isSuccessful) {
                val body = response.body()?.festival
                if (body != null) Result.Success(body)
                else Result.Error("Festival créé mais retour vide")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }
}
