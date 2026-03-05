package com.example.myapplication.data

import com.example.myapplication.model.Reservation
import com.example.myapplication.network.ApiService

class ReservationRepository(private val api: ApiService) {

    suspend fun getReservationsByFestival(festivalId: Int): Result<List<Reservation>> {
        return try {
            val response = api.getReservationsByFestival(festivalId)
            if (response.isSuccessful) {
                Result.Success(response.body() ?: emptyList())
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
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
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }
}
