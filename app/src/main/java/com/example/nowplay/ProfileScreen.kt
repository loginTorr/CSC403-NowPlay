package com.example.nowplay

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult // pfp
import androidx.activity.result.contract.ActivityResultContracts     // pfp
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateListOf          // ← added
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore       // KTX for Firebase.firestore

// global holder for the tapped‐post
var currentPost: Post? = null

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ProfileScreenFunction(navController: NavHostController) {

    val posts = remember { mutableStateListOf<Post>() }
    val context = LocalContext.current

    val database = Firebase.firestore
    val user     = FirebaseAuth.getInstance().currentUser
    val postCollection    = database.collection("/Users/${user?.uid}/Posts")
    val friendsCollection = database.collection("/Users/${user?.uid}/Friends")

    var numPosts   by rememberSaveable { mutableIntStateOf(0) }
    var numFriends by rememberSaveable { mutableIntStateOf(0) }
    var tempUser   by remember { mutableStateOf(User()) }
    var username   by remember { mutableStateOf("") }
    var bio        by remember { mutableStateOf("") }
    var location   by remember { mutableStateOf("") }


    var profileImageUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            val base64Image = encodeImageToBase64(context, it)
            uploadImageToImgur(
                base64Image,
                context,
                onSuccess = { resultUrl ->
                    profileImageUrl = resultUrl
                    user?.uid?.let { uid ->
                        database.collection("Users").document(uid)
                            .update("profileImageUrl", resultUrl)
                    }
                },
                onError = { err -> Log.e("ImgurUpload", "Error: $err") }
            )
        }
    }

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            database.collection("Users").document(uid).get()
                .addOnSuccessListener { doc ->
                    doc.getString("profileImageUrl")?.let { profileImageUrl = it }
                }
        }
    }


    LaunchedEffect(Unit) {
        friendsCollection.get().addOnSuccessListener { docs ->
            numFriends = docs.size()
        }
    }

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            database.collection("Users").document(uid).get()
                .addOnSuccessListener { doc ->
                    doc.toObject(User::class.java)?.let { u ->
                        tempUser = u
                        username = u.username.orEmpty()
                        bio = u.bio.orEmpty()
                        location = u.location.orEmpty()
                    }
                }
        }
    }


    LaunchedEffect(Unit) {
        postCollection.get().addOnSuccessListener { documents ->
            posts.clear()
            for (doc in documents) {
                doc.toObject(Post::class.java).let { p ->
                    posts.add(p)
                }
            }
            posts.sortByDescending { it.timeStamp }
            numPosts = posts.size
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Settings button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 30.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Button(
                    onClick = { navController.navigate(SettingsScreen) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector   = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile picture box
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") }
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl.isNullOrBlank()) {
                    Icon(
                        imageVector   = Icons.Default.AccountCircle,
                        contentDescription = "Default Profile Picture",
                        tint = Color.LightGray,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier     = Modifier.fillMaxSize()
                    )
                }
            }

            // Username
            Text(
                text = username,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp),
                fontSize  = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = bio,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                fontSize  = 15.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = location,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                fontSize  = 15.sp
            )

            // Counts row
            Row(
                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(80.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(numPosts.toString(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold)
                    Text("NowPlays", color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(numFriends.toString(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Friends", color = Color.Gray)
                }
            }

            // Share Profile
            Button(
                onClick = { /*…*/ },
                content = { Text("Share Profile", color = Color.White) },
                colors  = ButtonDefaults.buttonColors(Color.Gray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            )

            // Posts grid or “no posts” state
            if (numPosts == 0) {
                Column {
                    Text(
                        "Wow, it's really quiet in here!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 64.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Button(
                        onClick = { navController.navigate(PostScreen) },
                        content = {
                            Text("Add your NowPlaying.",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold)
                        },
                        colors = ButtonDefaults.buttonColors(Color.White),
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    contentPadding        = PaddingValues(vertical = 8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts.size) { index ->
                        AsyncImage(
                            model           = posts[index].songPicture,
                            contentDescription = "Song Picture",
                            modifier = Modifier
                                .fillMaxSize()
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
        enter   = slideInHorizontally { it },
        exit    = slideOutHorizontally { it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(26, 27, 28))
        ) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable { navController.navigate(ProfileScreen) }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                    Text(
                        "My NowPlaying.",
                        modifier = Modifier.align(Alignment.Center),
                        color    = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = currentPost?.timeStamp
                        .toString()
                        .removeRange(10, 23),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color    = Color.Gray,
                    fontSize = 15.sp
                )

                AsyncImage(
                    model = currentPost?.songPicture,
                    contentDescription = "Song Picture",
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .size(400.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop,
                )

                Text(
                    text = "${currentPost?.songName}",
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp),
                    color     = Color.White,
                    fontSize  = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${currentPost?.artistName}",
                    modifier = Modifier
                        .padding(start = 16.dp, top = 5.dp),
                    color    = Color.Gray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
