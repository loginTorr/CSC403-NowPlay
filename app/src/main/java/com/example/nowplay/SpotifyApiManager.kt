package com.example.nowplay

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SpotifyApiManager(private val accessToken: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)  // Add reasonable timeouts
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getCurrentPlaying(): SpotifySong? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("SPOTIFY", "Starting API call with token: ${accessToken.take(5)}...")

                val request = Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/currently-playing")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val response = client.newCall(request).execute()
                val responseCode = response.code
                Log.d("SPOTIFY", "Response code: $responseCode")

                when (responseCode) {
                    200 -> {
                        val body = response.body?.string()
                        Log.d("SPOTIFY", "Response body preview: ${body?.take(100)}...")

                        if (body == null) {
                            Log.e("SPOTIFY", "Response body is null despite 200 status")
                            return@withContext null
                        }

                        try {
                            val json = JSONObject(body)

                            // Check if item exists and is not null
                            if (!json.has("item") || json.isNull("item")) {
                                Log.d("SPOTIFY", "Response has no 'item' or it's null")
                                return@withContext null
                            }

                            val item = json.getJSONObject("item")
                            val name = item.getString("name")

                            // Safely get first artist or use "Unknown Artist"
                            val artistsArray = item.getJSONArray("artists")
                            val artist = if (artistsArray.length() > 0) {
                                artistsArray.getJSONObject(0).getString("name")
                            } else {
                                "Unknown Artist"
                            }

                            // Get album details safely
                            val album = item.getJSONObject("album")
                            val albumName = album.getString("name")

                            // Get image URL safely - use first image or a placeholder
                            val imagesArray = album.getJSONArray("images")
                            val albumImage = if (imagesArray.length() > 0) {
                                imagesArray.getJSONObject(0).getString("url")
                            } else {
                                "https://placeholder.com/400" // Placeholder image
                            }

                            Log.d("SPOTIFY", "Successfully parsed song: $name by $artist")
                            return@withContext SpotifySong(name, artist, albumName, albumImage)
                        } catch (e: Exception) {
                            Log.e("SPOTIFY", "JSON parsing error", e)
                            return@withContext null
                        }
                    }
                    204 -> {
                        Log.d("SPOTIFY", "No song is currently playing (204 No Content)")
                        return@withContext null
                    }
                    401 -> {
                        Log.e("SPOTIFY", "Unauthorized - token may be expired")
                        return@withContext null
                    }
                    else -> {
                        val errorBody = response.body?.string()
                        Log.e("SPOTIFY", "Error response: $responseCode, body: $errorBody")
                        return@withContext null
                    }
                }
            } catch (e: Exception) {
                Log.e("SPOTIFY", "Network or other exception", e)
                return@withContext null
            }
        }
    }
}

data class SpotifySong(
    val name: String,
    val artist: String,
    val album: String,
    val imageUrl: String
)