package com.example.nowplay

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreenFunction(
    spotifyAuthManager: SpotifyAuthManager,
    initialAccessToken: String?
) {
    val scrollState = rememberScrollState()
    var currentSong by remember { mutableStateOf<SpotifySong?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var initialLoading by remember { mutableStateOf(false) }
    var accessToken by remember { mutableStateOf(initialAccessToken) }

    LaunchedEffect(initialAccessToken) {
        accessToken = spotifyAuthManager.getAccessToken()
        if (accessToken != null) {
            initialLoading = true
            val first = SpotifyApiManager(accessToken!!).getCurrentPlaying()
            currentSong = first
            errorMessage = if (first != null) null else "No song playing"
            initialLoading = false

            while (true) {
                delay(5000)
                val freshToken = spotifyAuthManager.getAccessToken()
                if (freshToken != null) {
                    accessToken = freshToken
                    val updated = SpotifyApiManager(freshToken).getCurrentPlaying()
                    if (updated != null) {
                        currentSong = updated
                        errorMessage = null
                    }
                } else {
                    errorMessage = "Failed to refresh token"
                    break
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(26, 27, 28),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* dummy */ },
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = "Post",
                    modifier = Modifier.size(50.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
            Spacer(modifier = Modifier.height(24.dp))

            if (accessToken == null) {
                Button(
                    onClick = { spotifyAuthManager.authorize() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text("Connect to Spotify")
                }
            } else if (initialLoading) {
                CircularProgressIndicator(color = Color(0xFF1DB954))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading...", color = Color.White)
            } else if (currentSong != null) {
                Text(
                    text = "${currentSong!!.name} - ${currentSong!!.artist}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                AsyncImage(
                    model = currentSong!!.imageUrl,
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .size(300.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentSong!!.album,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.LightGray
                )
            } else {
                Text(
                    text = errorMessage ?: "No song playing",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}
