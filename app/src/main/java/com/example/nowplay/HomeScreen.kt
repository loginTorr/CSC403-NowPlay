package com.example.nowplay

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date


fun getFriendsPosts(): List<Post> {
    return listOf(
        Post("NotNick", "Weird Fishes","RadioHead", "In Rainbows", "https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("Aiden", "Stir Fry", "Migos", "Culture II","https://i.scdn.co/image/ab67616d0000b273e43e574f285798733979ba66"),
        Post("Tommy", "Dance, Dance", "Fall Out Boy", "From Under The Cork Tree", "https://i.scdn.co/image/ab67616d0000b27371565eda831124be86c603d5"),
        Post("MoseyAlong", "Washing Machine Heart", "Mitski", "Be The Cowboy","https://i.scdn.co/image/ab67616d0000b273c428f67b4a9b7e1114dfc117"),
        Post("5", "sampleSong", "sampleArtist", "Weird Fishes","https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("6", "sampleSong", "sampleArtist", "Weird Fishes","https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("7", "sampleSong", "sampleArtist", "Weird Fishes","https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
        Post("8", "sampleSong", "sampleArtist", "Weird Fishes","https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"),
    )
}


suspend fun getUserPost(): Post? {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
        val db = Firebase.firestore
        val snapshot = db.collection("CurrentPost").document("current").get().await()
        return if (snapshot.exists()) snapshot.toObject(Post::class.java) else null
    }
    return null
}




/*
fun grabUserPostValues(userId: String, songName: String, artistName: String, albumName: String, songPicture: String, timeStamp: Date? = Date()): MutableState<Post> {
    var UserPost by remember { mutableStateOf(Post("","","","","",null))}
    UserPost = Post(userId, songName, artistName, albumName, songPicture, timeStamp)
    return(UserPost)
}
*/

fun addUserPostToDatabase(userId: String, songName: String, artistName: String, albumName: String, image: String, timeStamp: Date? = Date()) {
    println("songName: $songName " + "artistName: $artistName " + "albumName: $albumName" + "image: $image" )
}

@Composable
fun HomeScreenFunction() {
    var userPost by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val samplePosts = remember { getFriendsPosts() } // Only call once
    var friendPosts by remember { mutableStateOf<List<Post>>(emptyList()) }


    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val db = Firebase.firestore

                val UserDoc = db.collection("Users")
                    .document(user.uid)
                    .collection("CurrentPost")
                    .document("current")
                    .get()
                    .await()

                if (UserDoc.exists() && UserDoc.toObject(Post::class.java) != null) {
                    userPost = listOf(UserDoc.toObject(Post::class.java)!!)
                } else {
                    Log.d("POST FINDER", "No current post found")
                }

                val FriendIDs = db.collection("Users")
                    .document(user.uid)
                    .collection("Friends")
                    .get()
                    .await()

                val tempFriendPosts = mutableListOf<Post>()

                for (friendDoc in FriendIDs.documents) {
                    try {
                        val friendId = friendDoc.id
                        Log.d("FRIEND", "Processing friend: $friendId")

                        // Fetch this friend's current post
                        val friendPostDoc = db.collection("Users")
                            .document(friendId)
                            .collection("CurrentPost")
                            .document("current")
                            .get()
                            .await()

                        if (friendPostDoc.exists()) {
                            val friendPost = friendPostDoc.toObject(Post::class.java)
                            if (friendPost != null) {
                                tempFriendPosts.add(friendPost)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FRIEND_POST_ERROR", "Failed to fetch post for friend: ${e.message}")
                    }
                }
                friendPosts = tempFriendPosts

            }
        } catch (e: Exception) {
            Log.e("DATA_LOAD_ERROR", "Error loading data: ${e.message}")
        } finally {
            isLoading = false
        }

    }



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
        Text(
            text = "NowPlaying.",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
    Spacer(modifier = Modifier.height(20.dp))

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White
            )
        }
    } else {
        DisplayHomePageFeed(userPost, friendPosts)
    }
}


@Composable
fun DisplayHomePageFeed(UserPosts: List<Post>, posts: List<Post>) {

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            LazyRow(
                modifier = Modifier.fillMaxSize()
                    .height(230.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top
            ) {
                items(UserPosts, key = { Post -> Post.timeStamp }) { Post ->
                    UserItem(Post)
                }
            }
        }
        items(posts, key = { Post -> Post.userId }) { Post ->
            PostFriendItems(Post)
        }
    }

}


@Composable
fun UserItem(post: Post) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .size(200.dp)
            .aspectRatio(1f)
            .clickable { }

    ) {
        AsyncImage(
            model = post.songPicture,
            contentDescription = "Song Picture",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop

        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .padding(8.dp)
                .background(Color.Gray.copy(alpha = 0.7f), shape = RoundedCornerShape(10.dp))
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
}

@Composable
fun PostFriendItems(post: Post) {
    var isFullSizeSongPicture by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(26, 27, 28))
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
                .aspectRatio(0.8f)
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
                            .clip(RoundedCornerShape(12.dp)),
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
                .offset(y = 150.dp), // Align with main content
            verticalArrangement = Arrangement.spacedBy(4.dp) // Reduce spacing here

        ) {
            IconButton (
                onClick = {/* TODO: Implement button functionality */ },
            )
            {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription =  "",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                )
            }



            IconButton (
                onClick = {/* TODO: Implement button functionality */ },

                )
            {
                Icon(
                    Icons.Filled.AddCircle,
                    contentDescription =  "",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                )
            }


            IconButton (
                onClick = {/* TODO: Implement button functionality */ },

                )
            {
                Icon(
                    Icons.Filled.Face,
                    contentDescription =  "",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                )
            }
        }
    }
}



