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
import okhttp3.*
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


    fun authorize() {
        // 1) Generate & save PKCE verifier/challenge
        val codeVerifier  = CodeVerifierUtil.generateRandomCodeVerifier()
        val codeChallenge = CodeVerifierUtil.deriveCodeVerifierChallenge(codeVerifier)
        context.getSharedPreferences("spotify_auth", Context.MODE_PRIVATE)
            .edit()
            .putString("code_verifier", codeVerifier)
            .apply()

        // 2) Build auth request
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri
        )
            .setScopes("user-read-currently-playing", "user-read-playback-state")
            .setCodeVerifier(codeVerifier, codeChallenge, "S256")
            .build()

        // 3) Launch browser
        val intent = authService.getAuthorizationRequestIntent(request)
        context.startActivity(intent)
    }


    fun handleRedirectIntent(intent: Intent, callback: (Boolean, String?) -> Unit) {
        val uri = intent.data
        Log.d("SPOTIFY","Auth redirect URI: $uri")

        val code = uri?.getQueryParameter("code")
        val error = uri?.getQueryParameter("error")
        if (code != null) {
            Log.d("SPOTIFY","Got auth code: $code…")
            exchangeCodeForToken(code, callback)
        } else {
            Log.e("SPOTIFY","Auth error: $error")
            Toast.makeText(context, "Spotify auth error: $error", Toast.LENGTH_LONG).show()
            callback(false, null)
        }
    }


    private fun exchangeCodeForToken(code: String, callback: (Boolean, String?) -> Unit) {
        val prefs    = context.getSharedPreferences("spotify_auth", Context.MODE_PRIVATE)
        val verifier = prefs.getString("code_verifier", null)
        if (verifier == null) {
            Log.e("SPOTIFY", "Missing PKCE verifier")
            return callback(false, null)
        }

        val form = FormBody.Builder()
            .add("grant_type",    "authorization_code")
            .add("code",          code)
            .add("redirect_uri",  redirectUri.toString())
            .add("client_id",     clientId)
            .add("code_verifier", verifier)
            .build()

        val req = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(form)
            .build()

        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
            .newCall(req)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("SPOTIFY", "Token exchange failure", e)
                    runOnMain { callback(false, null) }
                }
                override fun onResponse(call: Call, resp: Response) {
                    resp.use {
                        if (!resp.isSuccessful) {
                            Log.e("SPOTIFY", "Exchange HTTP ${resp.code}")
                            return runOnMain { callback(false, null) }
                        }
                        val json = JSONObject(resp.body!!.string())
                        val token = json.getString("access_token")
                        // Persist and return
                        prefs.edit().putString("access_token", token).apply()
                        Log.d("SPOTIFY", "Got token: ${token.take(8)}…")
                        runOnMain { callback(true, token) }
                    }
                }
            })
    }

    //post back on the UI thread
    private fun runOnMain(block: () -> Unit) {
        (context as? Activity)?.runOnUiThread(block)
    }


    suspend fun testSpotifyConnection(token: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder()
                    .url("https://api.spotify.com/v1/me")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val response = client.newCall(request).execute()
                Log.d("SPOTIFY", "Test HTTP ${response.code}")
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("SPOTIFY", "Connection test error", e)
                false
            }
        }
}
