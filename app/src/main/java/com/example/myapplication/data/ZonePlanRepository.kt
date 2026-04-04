package com.example.myapplication.data

import com.example.myapplication.model.ZonePlan
import com.example.myapplication.model.ZonePlanInput
import com.example.myapplication.model.ZonePlanUpdateInput
import com.example.myapplication.network.ApiService

class ZonePlanRepository(private val api: ApiService) {

    suspend fun getZonePlansByFestival(festivalId: Int): Result<List<ZonePlan>> {
        return try {
            val response = api.getZonePlans(festivalId)
            if (response.isSuccessful) {
                Result.Success(response.body().orEmpty())
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun createZonePlan(input: ZonePlanInput): Result<ZonePlan> {
        return try {
            val response = api.createZonePlan(input)
            if (response.isSuccessful) {
                val body = response.body()?.zonePlan
                if (body != null) Result.Success(body) else Result.Error("Zone plan créée mais retour vide")
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun updateZonePlan(id: Int, input: ZonePlanUpdateInput): Result<ZonePlan> {
        return try {
            val response = api.updateZonePlan(id, input)
            if (response.isSuccessful) {
                val body = response.body()?.zonePlan
                if (body != null) Result.Success(body) else Result.Error("Zone plan mise à jour mais retour vide")
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun deleteZonePlan(id: Int): Result<Unit> {
        return try {
            val response = api.deleteZonePlan(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }
}
