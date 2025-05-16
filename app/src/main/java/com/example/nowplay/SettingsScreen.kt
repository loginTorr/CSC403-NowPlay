package com.example.nowplay

import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

data class Notifications(
    var friendsPosts: Boolean = true,
    var friendRequests: Boolean = true,
    var newFriends: Boolean = true,
    var likes: Boolean = true,
    var comments: Boolean = true
)

@Composable
fun SettingsScreenFunction(navController: NavHostController) {
    val context = LocalContext.current

    // Set Up Database
    val database = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser
    var tempUser by remember { mutableStateOf(User()) }
    var username by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { _ ->
            val userReference = database.document("/Users/${user.uid}")
            userReference.get().addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    tempUser = document.toObject(User::class.java)!!
                    username = tempUser.username.orEmpty()
                    profileImageUrl = tempUser.profileImageUrl.orEmpty()
                }
            }
        }
    }

    // for the sliding settings page
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(Color(26, 27, 28))
        ) {
            Column (
                modifier = Modifier.padding(start = 16.dp, end = 16.dp).fillMaxSize()
            ){
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.align(CenterStart).size(24.dp).clickable { navController.navigate(ProfileScreen) }, tint = Color.White)
                    Text("Settings", modifier = Modifier.align(Center), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.DarkGray)
                        .clickable { navController.navigate(EditProfileScreen) },
                    contentAlignment = Center
                ) {
                    when {
                        profileImageUrl.isBlank() -> {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(75.dp)
                                    .align(CenterStart),
                                tint = Color.LightGray
                            )
                        }
                        else -> {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .clip(CircleShape)
                                    .size(75.dp)
                                    .align(CenterStart),
                            )
                        }
                    }
                    Text(username, modifier = Modifier.align(CenterStart).padding(start = 95.dp), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                            .align(CenterEnd),
                    )
                }
                Text("SETTINGS", modifier = Modifier.padding(top = 16.dp), color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.DarkGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(NotificationScreen) },
                        contentAlignment = Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notification Symbol",
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                                .align(CenterStart),
                            tint = Color.White
                        )
                        Text("Notifications", modifier = Modifier.align(CenterStart).padding(start = 44.dp), color = Color.White, fontSize = 15.sp)
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Go",
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                                .align(CenterEnd),
                        )
                    }
                    HorizontalDivider()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(DeleteAccountScreen) },
                        contentAlignment = Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                                .align(CenterStart),
                            tint = Color.White
                        )
                        Text("Delete Account", modifier = Modifier.align(CenterStart).padding(start = 44.dp), color = Color.White, fontSize = 15.sp)
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Go",
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                                .align(CenterEnd),
                        )
                    }
                }
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    content = { Text("Logout", color = Color.Red, fontSize = 20.sp) },
                    colors = ButtonDefaults.buttonColors(Color.DarkGray),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun EditProfileScreenFunction(navController: NavHostController) {
    // Set Up Database
    val database = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser

    var tempUser by remember { mutableStateOf(User()) }
    var firstName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { _ ->
            val userReference = database.document("/Users/${user.uid}")
            userReference.get().addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    tempUser = document.toObject(User::class.java)!!
                    //println("${tempUser.firstName}, ${tempUser.username}, ${tempUser.bio}, ${tempUser.location}")
                    firstName = tempUser.firstName.orEmpty()
                    username = tempUser.username.orEmpty()
                    bio = tempUser.bio.orEmpty()
                    location = tempUser.location.orEmpty()
                    profileImageUrl = tempUser.profileImageUrl.orEmpty()
                }
            }
        }
    }

    var saveButtonColor by remember { mutableStateOf(Color.DarkGray) }
    var profileChanges by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(26, 27, 28)),
        contentAlignment = Center
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp).fillMaxSize(),
            horizontalAlignment = CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Center
            ) {
                Text("Cancel", modifier = Modifier.align(CenterStart).clickable { navController.navigate(SettingsScreen) }, color = Color.White, fontSize = 15.sp)
                Text("Edit Profile", modifier = Modifier.align(Center), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Save", modifier = Modifier.align(CenterEnd).clickable { if (profileChanges) updateProfile(firstName, username, bio, location, navController) }, color = saveButtonColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = Color.DarkGray)
            when {
                profileImageUrl.isBlank() -> {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .padding(20.dp)
                            .size(150.dp),
                        tint = Color.LightGray
                    )
                }
                else -> {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(20.dp)
                            .clip(CircleShape)
                            .size(150.dp),
                    )
                }
            }
            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
            ) {
                Text("First Name", modifier = Modifier.align(CenterStart), color = Color.White, fontSize = 18.sp)
                BasicTextField(
                    value = firstName,
                    onValueChange = {
                        profileChanges = true
                        saveButtonColor = Color.White
                        if (it.length <= 20 && it.isNotEmpty()) {
                            firstName = it
                        }
                        else if (it.isEmpty()) {
                            firstName = it
                            profileChanges = false
                            saveButtonColor = Color.DarkGray
                        }
                                    },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(start = 125.dp),
                    decorationBox = { innerTextField ->
                        if (firstName.isEmpty()) {
                            Text(
                                text = "First Name",
                                color = Color.DarkGray,
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(bottom = 10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
            ) {
                Text("Username", modifier = Modifier.align(CenterStart), color = Color.White, fontSize = 18.sp)
                BasicTextField(
                    value = username,
                    onValueChange = {
                        profileChanges = true
                        saveButtonColor = Color.White
                        if (it.length <= 20 && it.isNotEmpty()) {
                            username = it
                        }
                        else if (it.isEmpty()) {
                            username = it
                            profileChanges = false
                            saveButtonColor = Color.DarkGray
                        }
                                    },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(start = 125.dp),
                    decorationBox = { innerTextField ->
                        if (username.isEmpty()) {
                            Text(
                                text = "Username",
                                color = Color.DarkGray,
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(bottom = 10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .padding(bottom = 10.dp),
            ) {
                Text("Bio", modifier = Modifier.align(TopStart), color = Color.White, fontSize = 18.sp)
                BasicTextField(
                    value = bio,
                    onValueChange = {
                        profileChanges = true
                        saveButtonColor = Color.White
                        if (it.length <= 100) bio = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(start = 125.dp),
                    decorationBox = { innerTextField ->
                        if (bio.isEmpty()) {
                            Text(
                                text = "Bio",
                                color = Color.DarkGray,
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(bottom = 10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
            ) {
                Text("Location", modifier = Modifier.align(CenterStart), color = Color.White, fontSize = 18.sp)
                BasicTextField(
                    value = location,
                    onValueChange = {
                        profileChanges = true
                        saveButtonColor = Color.White
                        if (it.length <= 20) location = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(start = 125.dp),
                    decorationBox = { innerTextField ->
                        if (location.isEmpty()) {
                            Text(
                                text = "Location",
                                color = Color.DarkGray,
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

fun updateProfile(firstName: String, username: String, bio: String, location: String, navController: NavHostController) {
    val database = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser
    val userReference = database.document("/Users/${user?.uid}")
    userReference.update("firstName", firstName, "username", username, "bio", bio, "location", location)
    navController.navigate(SettingsScreen)
}

@Composable
fun NotificationScreenFunction(navController: NavHostController) {
    val database = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser
    val settingsReference = database.collection("/Users/${user?.uid}/Settings")

    var notifications by remember { mutableStateOf(Notifications()) }
    var friendsPosts by remember { mutableStateOf(false) }
    var friendRequests by remember { mutableStateOf(false) }
    var newFriends by remember { mutableStateOf(false) }
    var likes by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        settingsReference.document("Notification_Settings").get().addOnSuccessListener { document ->
            if (document.exists()) {
                notifications = document.toObject(Notifications::class.java)!!
                friendsPosts = notifications.friendsPosts
                friendRequests = notifications.friendRequests
                newFriends = notifications.newFriends
                likes = notifications.likes
                comments = notifications.comments
            }
            else {
                settingsReference.document("Notification_Settings").set(Notifications())
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color(26, 27, 28))
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp).fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.align(CenterStart).size(24.dp)
                        .clickable { navController.navigate(SettingsScreen) },
                    tint = Color.White
                )
                Text(
                    "Notifications",
                    modifier = Modifier.align(Center),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text("POSTS", modifier = Modifier.padding(top = 16.dp), color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.DarkGray)
            ) {
                Icon(
                    Icons.Default.AccountBox,
                    contentDescription = "Account Box Symbol",
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp),
                    tint = Color.White
                )
                Text("Friends' Posts", modifier = Modifier.align(CenterStart).padding(start = 44.dp), color = Color.White, fontSize = 15.sp)
                Switch(
                    checked = friendsPosts,
                    onCheckedChange = {
                        friendsPosts = it
                        settingsReference.document("Notification_Settings").update("friendsPosts", friendsPosts)
                                      },
                    modifier = Modifier.align(CenterEnd). padding(end = 10.dp)
                )
            }

            Text("INTERACTIONS", modifier = Modifier.padding(top = 16.dp), color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.DarkGray)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Center
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = "People",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                            .align(CenterStart),
                        tint = Color.White
                    )
                    Text("Friend Requests", modifier = Modifier.align(CenterStart).padding(start = 44.dp), color = Color.White, fontSize = 15.sp)
                    Switch(
                        checked = friendRequests,
                        onCheckedChange = {
                            friendRequests = it
                            settingsReference.document("Notification_Settings").update("friendRequests", friendRequests)
                        },
                        modifier = Modifier.align(CenterEnd). padding(end = 10.dp)
                    )
                }
                HorizontalDivider()
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Center
                ) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        contentDescription = "Add Circle Outline",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                            .align(CenterStart),
                        tint = Color.White
                    )
                    Text("New Friends", modifier = Modifier.align(CenterStart).padding(start = 44.dp), color = Color.White, fontSize = 15.sp)
                    Switch(
                        checked = newFriends,
                        onCheckedChange = {
                            newFriends = it
                            settingsReference.document("Notification_Settings").update("newFriends", newFriends)
                        },
                        modifier = Modifier.align(CenterEnd). padding(end = 10.dp)
                    )
                }
                HorizontalDivider()
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Center
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = "Thumb Up",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                            .align(CenterStart),
                        tint = Color.White
                    )
                    Text("Likes", modifier = Modifier.align(CenterStart).padding(start = 44.dp), color = Color.White, fontSize = 15.sp)
                    Switch(
                        checked = likes,
                        onCheckedChange = {
                            likes = it
                            settingsReference.document("Notification_Settings").update("likes", likes)
                        },
                        modifier = Modifier.align(CenterEnd). padding(end = 10.dp)
                    )
                }
                HorizontalDivider()
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Center
                ) {
                    Icon(
                        Icons.Default.ModeComment,
                        contentDescription = "Comment",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                            .align(CenterStart),
                        tint = Color.White
                    )
                    Text("Comments", modifier = Modifier.align(CenterStart).padding(start = 44.dp), color = Color.White, fontSize = 15.sp)
                    Switch(
                        checked = comments,
                        onCheckedChange = {
                            comments = it
                            settingsReference.document("Notification_Settings").update("comments", comments)
                        },
                        modifier = Modifier.align(CenterEnd). padding(end = 10.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreenFunction(navController: NavHostController) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val deleteUser = remember { mutableStateOf(false) }

    if (deleteUser.value) {
        val database = Firebase.firestore
        val user = Firebase.auth.currentUser!!
        val userReference = database.document("/Users/${user.uid}")
        userReference.delete()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
        user.delete()
    }

    if (showDialog.value) {
        BasicAlertDialog(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .size(width = 300.dp, height = 200.dp)
                .background(Color.White),
            onDismissRequest = { showDialog.value = false }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Center
            ) {
                Text("Are you sure you want to delete your account?", modifier = Modifier.padding(20.dp).align(TopCenter),color = Color.Unspecified, fontSize = 20.sp, textAlign = TextAlign.Center)
                Row(
                    modifier = Modifier.align(BottomCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        modifier = Modifier
                            .padding(15.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        onClick = { showDialog.value = false }
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 20.sp)
                    }
                    Button(
                        modifier = Modifier
                            .padding(15.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        onClick = { deleteUser.value = true }
                    ) {
                        Text("Confirm", color = Color.White, fontSize = 20.sp)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color(26, 27, 28))
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp).fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.align(CenterStart).size(24.dp)
                        .clickable { navController.navigate(SettingsScreen) },
                    tint = Color.White
                )
                Text(
                    "Delete Account",
                    modifier = Modifier.align(Center),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.DarkGray)
                    .clickable { showDialog.value = true },
                contentAlignment = Center
            ) {
                Text("Delete Account", modifier = Modifier.align(Center).padding(10.dp), color = Color.Red, fontSize = 20.sp)
            }
        }
    }
}