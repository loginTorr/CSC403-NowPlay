package com.example.nowplay

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.google.firebase.Timestamp


data class Post(
    val userId: String,
    val songName: String,
    val artistName: String,
    val albumName: String,
    val songPicture: String,
    val timestamp: Timestamp? = null
)

fun getFriendsPosts(): List<Post> {
    return listOf(
        Post("1", "Weird Fishes","RadioHead", "Weird Fishes", "https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("2", "Stir Fry", "Migos", "Culture II","https://i.scdn.co/image/ab67616d0000b273e43e574f285798733979ba66"),
        Post("3", "sampleSong", "sampleArtist", "Weird Fishes", "https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("4", "sampleSong", "sampleArtist", "Weird Fishes","https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("5", "sampleSong", "sampleArtist", "Weird Fishes","https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("6", "sampleSong", "sampleArtist", "Weird Fishes","https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("7", "sampleSong", "sampleArtist", "Weird Fishes","https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("8", "sampleSong", "sampleArtist", "Weird Fishes","https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
    )
}

@Composable
fun HomeScreenFunction() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "",
            color = Color.White
        )
    }
    //NowPlay Logo
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "NowPlay.",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
    Spacer(modifier = Modifier.height(20.dp))

    var samplePosts = getFriendsPosts()
    DisplayFriendPosts(samplePosts)
}


@Composable
fun DisplayFriendPosts(posts: List<Post>) {
    LazyColumn (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        items(posts, key = { HomeScreenPost -> HomeScreenPost.userId }) { HomeScreenPost ->
            PostItems(HomeScreenPost)
        }
    }
}

@Composable
fun PostItems(post: Post) {
    var isFullSizeSongPicture by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black, shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // User Info (Static)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(50.dp),
                tint = Color.LightGray
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = post.userId,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(top = 56.dp) // Offset for user info
        ) {
            // Song Picture and Song Info Cards
            if (isFullSizeSongPicture) {
                // Full Size Song Picture
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    AsyncImage(
                        model = post.songPicture,
                        contentDescription = "Song Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Small Song Info Card
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                            .background(Color.Gray.copy(alpha = 0.7f), shape = RoundedCornerShape(10.dp))
                            .clickable { isFullSizeSongPicture = false }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = post.songName,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = post.artistName,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = post.albumName,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                // Full Size Song Info Card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray, shape = RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = post.songName,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = post.artistName,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = post.albumName,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Small Song Picture
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isFullSizeSongPicture = true }
                    ) {
                        AsyncImage(
                            model = post.songPicture,
                            contentDescription = "Song Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // Side Buttons (Positioned Absolutely)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .offset(y = 56.dp) // Align with main content
        ) {
            Button(
                onClick = { /* TODO: Implement button functionality */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Button 1")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* TODO: Implement button functionality */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Button 2")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* TODO: Implement button functionality */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Button 3")
            }
        }
    }
}

