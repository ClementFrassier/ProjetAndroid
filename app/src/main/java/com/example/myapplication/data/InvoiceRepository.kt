package com.example.myapplication.data

import com.example.myapplication.model.Invoice
import com.example.myapplication.network.ApiService

class InvoiceRepository(private val api: ApiService) {

    suspend fun getInvoiceByReservation(reservationId: Int): Result<Invoice?> {
        return try {
            val response = api.getInvoiceByReservation(reservationId)
            when {
                response.isSuccessful -> Result.Success(response.body())
                response.code() == 404 -> Result.Success(null)
                else -> Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun createInvoiceForReservation(reservationId: Int): Result<Invoice> {
        return try {
            val response = api.createInvoiceForReservation(reservationId)
            if (response.isSuccessful) {
                val body = response.body()?.facture
                if (body != null) Result.Success(body)
                else Result.Error("Facture créée mais retour vide")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun markInvoiceAsPaid(id: Int): Result<Invoice> {
        return try {
            val response = api.markInvoiceAsPaid(id)
            if (response.isSuccessful) {
                val body = response.body()?.facture
                if (body != null) Result.Success(body)
                else Result.Error("Erreur lors de la mise à jour")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }
}
