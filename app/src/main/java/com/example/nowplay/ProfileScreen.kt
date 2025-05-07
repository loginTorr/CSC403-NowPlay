package com.example.nowplay

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

var currentPost: Post? = null

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ProfileScreenFunction(username: String, navController: NavHostController) {
    val posts by remember { mutableStateOf(mutableListOf<Post>()) }

    // Set Up Database
    val database = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser
    val postCollection = database.collection("/Users/${user?.uid}/Posts")
    val friendsCollection = database.collection("/Users/${user?.uid}/Friends")
    var numPosts by rememberSaveable { mutableIntStateOf(0) }
    var numFriends by rememberSaveable { mutableIntStateOf(0) }

    // Count number of friends of user
    LaunchedEffect(key1 = true) {
        friendsCollection.get().addOnSuccessListener { documents ->
            for (document in documents) {
                numFriends++
            }
        }
    }

    // Count number of posts of user
    LaunchedEffect(key1 = true) {
        postCollection.get().addOnSuccessListener { documents ->
            for (document in documents) {
                numPosts++
                val post = Post(
                    userId = "${user?.uid}",
                    artistName = "artist",
                    albumName = "album",
                    songName = "song",
                    songPicture = "https://i.scdn.co/image/ab67616d0000b273de3c04b5fc750b68899b20a9"
                )
                posts.add(post)
            }
            posts.sortByDescending { it.timeStamp }
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Button(
                    onClick = { navController.navigate(SettingsScreen) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon (
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(100.dp),
                tint = Color.LightGray
            )

            Text(
                username,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(80.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(numPosts.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("NowPlays", color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(numFriends.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Friends", color = Color.Gray)
                }
            }

            Button(
                onClick = {  },
                content = { Text("Share Profile", color = Color.White) },
                colors = ButtonDefaults.buttonColors(Color.Gray),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp)
            )
            if (numPosts == 0) {
                Column {
                    Text(
                        "Wow, it's really quiet in here!",
                        color = Color.White, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 64.dp).align(Alignment.CenterHorizontally)
                    )
                    Button(
                        onClick = {
                            navController.navigate(PostScreen)
                        },
                        content = { Text("Add your NowPlaying.", color = Color.Black, fontWeight = FontWeight.Bold) },
                        colors = ButtonDefaults.buttonColors(Color.White),
                        modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally)
                    )
                }
            }
            else if (numPosts > 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts.size) { index ->
                        AsyncImage(
                            model = posts[index].songPicture,
                            contentDescription = "Song Picture",
                            modifier = Modifier.fillMaxSize()
                                .size(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Gray)
                                .clickable {
                                    currentPost = posts[index]
                                    navController.navigate(ViewPostScreen)
                                },
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewPostScreenFunction(navController: NavHostController) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(26, 27, 28))
        ){
            Column {
                Box (
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    Box(modifier = Modifier.align(Alignment.CenterStart).clickable { navController.navigate(ProfileScreen) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", modifier = Modifier.size(40.dp), tint = Color.White)
                    }
                    Text("My NowPlaying.", modifier = Modifier.align(Alignment.Center), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(currentPost?.timeStamp.toString().removeRange(10, 23), modifier = Modifier.align(Alignment.CenterHorizontally), color = Color.Gray, fontSize = 15.sp)
                AsyncImage(
                    model = currentPost?.songPicture,
                    contentDescription = "Song Picture",
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .size(400.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop,
                )
                Text("${currentPost?.songName}", modifier = Modifier.padding(start = 16.dp, top = 16.dp), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Text("${currentPost?.artistName}", modifier = Modifier.padding(start = 16.dp, top = 5.dp), color = Color.Gray, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}