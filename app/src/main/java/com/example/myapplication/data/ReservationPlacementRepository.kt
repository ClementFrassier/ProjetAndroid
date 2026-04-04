package com.example.myapplication.data

import com.example.myapplication.model.ReservationGamePlacement
import com.example.myapplication.model.ReservationGamePlacementCreateInput
import com.example.myapplication.model.ReservationGamePlacementUpdateInput
import com.example.myapplication.network.ApiService

class ReservationPlacementRepository(private val api: ApiService) {

    suspend fun getPlacementsByReservation(reservationId: Int): Result<List<ReservationGamePlacement>> {
        return try {
            val response = api.getReservationGamePlacements(reservationId)
            if (response.isSuccessful) {
                Result.Success(response.body().orEmpty())
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun createPlacement(input: ReservationGamePlacementCreateInput): Result<ReservationGamePlacement> {
        return try {
            val response = api.createReservationGamePlacement(input)
            if (response.isSuccessful) {
                val body = response.body()?.reservationGamePlacement
                if (body != null) Result.Success(body) else Result.Error("Placement créé mais retour vide")
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun updatePlacement(id: Int, input: ReservationGamePlacementUpdateInput): Result<ReservationGamePlacement> {
        return try {
            val response = api.updateReservationGamePlacement(id, input)
            if (response.isSuccessful) {
                val body = response.body()?.updatedPlacement ?: response.body()?.reservationGamePlacement
                if (body != null) Result.Success(body) else Result.Error("Placement mis à jour mais retour vide")
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun deletePlacement(id: Int): Result<Unit> {
        return try {
            val response = api.deleteReservationGamePlacement(id)
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
