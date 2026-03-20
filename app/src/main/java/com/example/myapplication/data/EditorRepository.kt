package com.example.myapplication.data

import com.example.myapplication.model.Editor
import com.example.myapplication.model.EditorDetail
import com.example.myapplication.model.EditorInput
import com.example.myapplication.network.ApiService

class EditorRepository(private val api: ApiService) {

    suspend fun getEditors(): Result<List<Editor>> {
        return try {
            val response = api.getEditors()
            if (response.isSuccessful) {
                Result.Success(response.body() ?: emptyList())
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun getEditorById(id: Int): Result<EditorDetail> {
        return try {
            val editorResponse = api.getEditorById(id)
            val gamesResponse = api.getEditorGames(id)
            if (editorResponse.isSuccessful) {
                val body = editorResponse.body()
                if (body != null) {
                    Result.Success(
                        body.copy(games = gamesResponse.body().orEmpty())
                    )
                }
                else Result.Error("Éditeur introuvable")
            } else {
                Result.Error("Erreur ${editorResponse.code()} : ${editorResponse.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun createEditor(editor: EditorInput): Result<EditorDetail> {
        return try {
            val response = api.createEditor(editor)
            if (response.isSuccessful) {
                val body = response.body()?.editeur
                if (body != null) Result.Success(body)
                else Result.Error("Erreur lors de la création d'éditeur")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun updateEditor(id: Int, editor: EditorInput): Result<EditorDetail> {
        return try {
            val response = api.updateEditor(id, editor)
            if (response.isSuccessful) {
                val body = response.body()?.editeur
                if (body != null) Result.Success(body)
                else Result.Error("Erreur lors de la modification de l'éditeur")
            } else {
                Result.Error("Erreur ${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Impossible de joindre le serveur : ${e.localizedMessage}")
        }
    }

    suspend fun deleteEditor(id: Int): Result<Unit> = Result.Error("Suppression non disponible côté API mobile")
}
