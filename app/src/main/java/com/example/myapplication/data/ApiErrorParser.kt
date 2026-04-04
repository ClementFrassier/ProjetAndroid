package com.example.myapplication.data

import org.json.JSONObject
import retrofit2.Response

internal fun Response<*>.toApiErrorMessage(defaultMessage: String? = null): String {
    val errorBody = try {
        errorBody()?.string()
    } catch (_: Exception) {
        null
    }

    val parsedMessage = errorBody
        ?.takeIf { it.isNotBlank() }
        ?.let { body ->
            runCatching {
                val json = JSONObject(body)
                json.optString("error").ifBlank {
                    json.optString("message")
                }
            }.getOrNull()
        }
        ?.takeIf { it.isNotBlank() }

    return parsedMessage ?: defaultMessage ?: "Erreur ${code()} : ${message()}"
}
