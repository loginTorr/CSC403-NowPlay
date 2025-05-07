package com.example.nowplay

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreenFunction(
    spotifyAuthManager: SpotifyAuthManager,
    accessToken: MutableState<String?>
) {
    val context = LocalContext.current
    val activity = context as Activity
    val scrollState = rememberScrollState()

    var currentSong by remember { mutableStateOf<SpotifySong?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var debugInfo by remember { mutableStateOf("Waiting for song fetch...") }
    val coroutineScope = rememberCoroutineScope()

    // Re-handle redirect if MainActivity didn't catch it
    val intentUri = activity.intent.data
    LaunchedEffect(intentUri) {
        intentUri?.let { uri ->
            if (uri.toString().startsWith("nowplay://callback")) {
                Log.d("SPOTIFY", "PostScreen caught redirect URI: $uri")
                spotifyAuthManager.handleRedirectIntent(activity.intent) { success, token ->
                    if (success && token != null) {
                        accessToken.value = token
                        Toast.makeText(context, "Spotify connected!", Toast.LENGTH_SHORT).show()
                        debugInfo = "Connected, token saved."
                    } else {
                        errorMessage = "Authorization failed"
                        debugInfo = "Redirect handling failed"
                    }
                }
            }
        }
    }

    // Fetch current song whenever token changes
    LaunchedEffect(accessToken.value) {
        if (accessToken.value == null) return@LaunchedEffect
        debugInfo = "Token available, testing connection..."
        isLoading = true
        try {
            val ok = spotifyAuthManager.testSpotifyConnection(accessToken.value!!)
            if (ok) debugInfo = "Connection OK, starting fetch loop"
            else {
                errorMessage = "Cannot connect to Spotify API"
                debugInfo = "Connection test failed"
            }
        } catch (e: Exception) {
            errorMessage = "Connection error: ${e.message}"
            debugInfo = "Test exception: ${e.message}"
        } finally {
            isLoading = false
        }

        // Periodic fetch
        while (accessToken.value != null) {
            isLoading = true
            debugInfo = "Fetching current song..."
            try {
                val api = SpotifyApiManager(accessToken.value!!)
                val song = api.getCurrentPlaying()
                if (song != null) {
                    currentSong = song
                    errorMessage = null
                    debugInfo = "Fetched: ${song.name}"
                } else {
                    currentSong = null
                    errorMessage = "No song playing"
                    debugInfo = "No song currently playing"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                debugInfo = "Fetch error: ${e.message}"
                Log.e("SPOTIFY", "Error fetching song", e)
            } finally {
                isLoading = false
            }
            delay(5000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Now Playing on Spotify",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (accessToken.value == null) {
            Button(
                onClick = { spotifyAuthManager.authorize() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Connect to Spotify")
            }
        } else {
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF1DB954))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...", color = Color.White)
            } else if (currentSong != null) {
                Text(
                    text = currentSong!!.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${currentSong!!.artist} — ${currentSong!!.album}",
                    fontSize = 16.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = currentSong!!.imageUrl,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch { /* Manual refresh same as loop */ }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text("Refresh")
                }
            } else {
                Text(
                    text = errorMessage ?: "No song playing",
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch { /* Manual check */ }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text("Check Again")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF424242))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Debug Info", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Token: ${accessToken.value?.take(10) ?: "None"}...", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(debugInfo, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
