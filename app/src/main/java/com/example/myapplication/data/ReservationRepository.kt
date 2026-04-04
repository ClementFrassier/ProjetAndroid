package com.example.myapplication.data

import com.example.myapplication.model.Reservation
import com.example.myapplication.model.ReservationCreateInput
import com.example.myapplication.model.ReservationUpdateInput
import com.example.myapplication.network.ApiService

class ReservationRepository(private val api: ApiService) {

    suspend fun getReservationsByFestival(festivalId: Int): Result<List<Reservation>> {
        return try {
            val response = api.getReservationsByFestival(festivalId)
            if (response.isSuccessful) {
                Result.Success(response.body() ?: emptyList())
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun getReservation(id: Int): Result<Reservation> {
        return try {
            val response = api.getReservation(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Réservation introuvable")
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun createReservation(reservation: ReservationCreateInput): Result<Reservation> {
        return try {
            val response = api.createReservation(reservation)
            if (response.isSuccessful) {
                val body = response.body()?.reservation
                if (body != null) Result.Success(body)
                else Result.Error("Erreur lors de la création")
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun updateReservation(id: Int, reservation: ReservationUpdateInput): Result<Reservation> {
        return try {
            val response = api.updateReservation(id, reservation)
            if (response.isSuccessful) {
                val body = response.body()?.reservation
                if (body != null) Result.Success(body)
                else Result.Error("Erreur lors de la mise à jour")
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun deleteReservation(id: Int): Result<Unit> {
        return try {
            val response = api.deleteReservation(id)
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
