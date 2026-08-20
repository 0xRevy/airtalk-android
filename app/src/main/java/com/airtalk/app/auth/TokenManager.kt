package com.airtalk.app.auth

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object TokenManager {

    private const val PREFS = "airtalk_auth"
    private const val KEY_TOKEN = "access_token"

    // Guest JWT provisioned for this app build. Works until the backend mints fresh
    // guest tokens; the app falls back to this whenever no stored token exists.
    private const val FALLBACK_GUEST_TOKEN =
        "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJhaXJ0YWxrLmxpdmUiLCJzdWIiOiI0N2FjYzY2Ny0wYzljLTRmNTktODRlYS05OTUxNWY0MGQ4MzAiLCJhY2wiOiJHVUVTVCJ9.v0VkgeQ2qfGctb48VKS5sNvQyZBYgPYI7fn7-zkudcXUmirOo5ZJC4GPz9gMwgd3H86D3kT-ZF6ZKT74k2hNQQ"

    // When your backend exposes a guest-mint endpoint (e.g. POST /login/guest ->
    // {"token": "<jwt>"}), set this and the app will fetch fresh tokens automatically.
    private const val GUEST_MINT_URL = ""

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun getToken(): String =
        prefs?.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() } ?: FALLBACK_GUEST_TOKEN

    fun storeToken(token: String) {
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun clearToken() {
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    /**
     * If a guest-mint endpoint is configured, fetch a fresh token. Returns true when
     * a usable token is available afterwards. Never throws; on any failure the
     * stored/fallback token remains in place.
     */
    fun ensureFreshToken(): Boolean {
        if (GUEST_MINT_URL.isBlank()) return true
        return try {
            val conn = URL(GUEST_MINT_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val token = json.optString("token").ifBlank { json.optString("accessToken") }
            if (token.isNotBlank()) {
                storeToken(token)
                true
            } else false
        } catch (e: Exception) {
            false
        } finally {
            // keep fallback
        }
    }
}