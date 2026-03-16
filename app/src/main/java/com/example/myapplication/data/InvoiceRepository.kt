package com.example.myapplication.data

import com.example.myapplication.model.Invoice
import com.example.myapplication.model.InvoiceCreateInput
import com.example.myapplication.network.ApiService

class InvoiceRepository(private val api: ApiService) {

    suspend fun getInvoices(festivalId: Int? = null, editorId: Int? = null, isPaid: Boolean? = null): Result<List<Invoice>> {
        return try {
            val response = api.getInvoices(festivalId, editorId, isPaid)
            if (response.isSuccessful) {
                Result.Success(response.body() ?: emptyList())
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun getInvoiceById(id: Int): Result<Invoice> {
        return try {
            val response = api.getInvoiceById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Facture introuvable")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun createInvoice(invoice: InvoiceCreateInput): Result<Invoice> {
        return try {
            val response = api.createInvoice(invoice)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Erreur lors de la création")
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
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Erreur lors de la mise à jour")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun deleteInvoice(id: Int): Result<Unit> {
        return try {
            val response = api.deleteInvoice(id)
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
