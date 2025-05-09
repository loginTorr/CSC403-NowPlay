package com.example.nowplay

import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun SettingsScreenFunction(navController: NavHostController) {
    val context = LocalContext.current

    // Set Up Database
    val database = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser
    var tempUser by remember { mutableStateOf(User()) }
    var username by remember { mutableStateOf("") }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { _ ->
            val userReference = database.document("/Users/${user.uid}")
            userReference.get().addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    tempUser = document.toObject(User::class.java)!!
                    username = tempUser.username.orEmpty()
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
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(75.dp)
                            .align(CenterStart),
                        tint = Color.LightGray
                    )
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
                            .clickable {  },
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
                            .clickable {  },
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
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .padding(20.dp)
                    .size(150.dp),
                tint = Color.LightGray
            )
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
                        if (it.length <= 20) firstName = it },
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
                        if (it.length <= 20) username = it },
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