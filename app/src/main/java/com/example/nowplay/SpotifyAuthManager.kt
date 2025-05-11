// SpotifyAuthManager.kt
package com.example.nowplay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.browser.BrowserAllowList
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SpotifyAuthManager(private val context: Context) {
    private val clientId = "84aaf42d76e04c039437c6c1033f7982"
    private val redirectUri = Uri.parse("nowplay://callback")

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse("https://accounts.spotify.com/authorize"),
        Uri.parse("https://accounts.spotify.com/api/token")
    )

    private val authService: AuthorizationService by lazy {
        AuthorizationService(
            context,
            AppAuthConfiguration.Builder()
                .setBrowserMatcher(BrowserAllowList(AnyBrowserMatcher.INSTANCE))
                .build()
        )
    }

    companion object {
        private const val PREFS_NAME = "spotify_auth"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }

    fun authorize() {
        // unchanged PKCE setup and redirect
        val codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier()
        val codeChallenge = CodeVerifierUtil.deriveCodeVerifierChallenge(codeVerifier)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("code_verifier", codeVerifier)
            .apply()

        val request = AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri
        )
            .setScopes("user-read-currently-playing", "user-read-playback-state")
            .setCodeVerifier(codeVerifier, codeChallenge, "S256")
            .build()

        val intent = authService.getAuthorizationRequestIntent(request)
        context.startActivity(intent)
    }

    fun handleRedirectIntent(intent: Intent, callback: (Boolean, String?) -> Unit) {
        val uri = intent.data
        Log.d("SPOTIFY", "Auth redirect URI: $uri")

        val code = uri?.getQueryParameter("code")
        val error = uri?.getQueryParameter("error")
        if (code != null) {
            exchangeCodeForToken(code, callback)
        } else {
            Log.e("SPOTIFY", "Auth error: $error")
            Toast.makeText(context, "Spotify auth error: $error", Toast.LENGTH_LONG).show()
            callback(false, null)
        }
    }

    private fun exchangeCodeForToken(code: String, callback: (Boolean, String?) -> Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val verifier = prefs.getString("code_verifier", null) ?: run {
            Log.e("SPOTIFY", "Missing PKCE verifier")
            return callback(false, null)
        }

        val formBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", redirectUri.toString())
            .add("client_id", clientId)
            .add("code_verifier", verifier)
            .build()

        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(formBody)
            .build()

        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
            .newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("SPOTIFY", "Token exchange failure", e)
                    runOnMain { callback(false, null) }
                }

                override fun onResponse(call: okhttp3.Call, resp: Response) {
                    resp.use {
                        if (!resp.isSuccessful) {
                            Log.e("SPOTIFY", "Exchange HTTP ${resp.code}")
                            return runOnMain { callback(false, null) }
                        }
                        val json = JSONObject(resp.body!!.string())
                        val accessToken = json.getString("access_token")
                        val refreshToken = json.getString("refresh_token")
                        val expiresIn = json.getLong("expires_in")
                        val expiresAt = System.currentTimeMillis() + expiresIn * 1000

                        prefs.edit()
                            .putString(KEY_ACCESS_TOKEN, accessToken)
                            .putString(KEY_REFRESH_TOKEN, refreshToken)
                            .putLong(KEY_EXPIRES_AT, expiresAt)
                            .apply()

                        runOnMain { callback(true, accessToken) }
                    }
                }
            })
    }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return@withContext if (accessToken != null) {
            if (System.currentTimeMillis() >= expiresAt) {
                if (refreshAccessToken()) prefs.getString(KEY_ACCESS_TOKEN, null) else null
            } else {
                accessToken
            }
        } else null
    }

    private suspend fun refreshAccessToken(): Boolean = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return@withContext false

        val formBody = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", clientId)
            .build()

        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(formBody)
            .build()

        try {
            val response = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
                .newCall(request)
                .execute()
            if (!response.isSuccessful) return@withContext false
            val json = JSONObject(response.body!!.string())
            val newToken = json.getString("access_token")
            val expiresIn = json.getLong("expires_in")
            val expiresAt = System.currentTimeMillis() + expiresIn * 1000

            val editor = prefs.edit()
                .putString(KEY_ACCESS_TOKEN, newToken)
                .putLong(KEY_EXPIRES_AT, expiresAt)
            if (json.has("refresh_token")) {
                editor.putString(KEY_REFRESH_TOKEN, json.getString("refresh_token"))
            }
            editor.apply()
            true
        } catch (e: Exception) {
            Log.e("SPOTIFY", "Failed to refresh token", e)
            false
        }
    }

    private fun runOnMain(block: () -> Unit) {
        (context as? Activity)?.runOnUiThread(block)
    }
}