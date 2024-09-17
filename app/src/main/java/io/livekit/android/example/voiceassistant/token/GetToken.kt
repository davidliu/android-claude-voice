package io.livekit.android.example.voiceassistant.token

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject

suspend fun getToken(tokenServerUrl: String): TokenResponse? {
    var tokenResponse: TokenResponse? = null
    try {
        HttpClient(Android).use {
            val body = it.get(urlString = tokenServerUrl)
                .bodyAsText()

            val json = JSONObject(body)
            tokenResponse = TokenResponse(
                token = json["token"] as String,
                url = json["url"] as String,
            )
        }
    } catch (e: Exception) {
        Log.e("GetToken", "Error getting from token server:", e)
    }

    return tokenResponse
}

data class TokenResponse(val token: String, val url: String)