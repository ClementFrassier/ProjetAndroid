package com.example.myapplication.data

import com.example.myapplication.model.CrmContactCreateInput
import com.example.myapplication.model.CrmFollowUp
import com.example.myapplication.model.CrmRow
import com.example.myapplication.model.CrmUpsertInput
import com.example.myapplication.network.ApiService

class CrmRepository(private val api: ApiService) {

    suspend fun getRowsByFestival(festivalId: Int): Result<List<CrmRow>> {
        return try {
            val response = api.getCrmRows(festivalId)
            if (response.isSuccessful) {
                Result.Success(response.body().orEmpty())
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun upsertStatus(input: CrmUpsertInput): Result<Unit> {
        return try {
            val response = api.upsertCrmStatus(input)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun addContact(input: CrmContactCreateInput): Result<Unit> {
        return try {
            val response = api.createCrmContact(input)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun getContacts(editorId: Int, festivalId: Int): Result<List<CrmFollowUp>> {
        return try {
            val response = api.getCrmContacts(editorId, festivalId)
            if (response.isSuccessful) {
                Result.Success(response.body().orEmpty())
            } else {
                Result.Error(response.toApiErrorMessage())
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }
}
