package com.example.nowplay

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreenFunction(
    spotifyAuthManager: SpotifyAuthManager,
    accessToken: MutableState<String?>
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var currentSong by remember { mutableStateOf<SpotifySong?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch song when token arrives
    LaunchedEffect(accessToken.value) {
        if (accessToken.value == null) return@LaunchedEffect
        isLoading = true
        val api = SpotifyApiManager(accessToken.value!!)
        val song = api.getCurrentPlaying()
        if (song != null) {
            currentSong = song
            errorMessage = null
        } else {
            currentSong = null
            errorMessage = "No song playing"
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen title
        Text(
            text = "Now Playing on Spotify",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (accessToken.value == null) {
            // Connect button
            Button(
                onClick = { spotifyAuthManager.authorize() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Connect to Spotify")
            }
        } else {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading...", color = Color.White)
                }
                currentSong != null -> {
                    // Artist
                    Text(
                        text = "${currentSong!!.name} - ${currentSong!!.artist}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Album Cover
                    AsyncImage(
                        model = currentSong!!.imageUrl,
                        contentDescription = "Album cover",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Album name
                    Text(
                        text = currentSong!!.album,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                else -> {
                    // No song or error message
                    Text(
                        text = errorMessage ?: "No song playing",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Refresh button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val api = SpotifyApiManager(accessToken.value!!)
                        val song = api.getCurrentPlaying()
                        if (song != null) {
                            currentSong = song
                            errorMessage = null
                        } else {
                            currentSong = null
                            errorMessage = "No song playing"
                        }
                        isLoading = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Refresh Now Playing")
            }
        }
    }
}
