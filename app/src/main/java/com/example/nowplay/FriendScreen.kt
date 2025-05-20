package com.example.nowplay

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.ArrowBack
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.filled.Search
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

@Composable
fun ProfileIcon(size: Dp = 32.dp, tint: Color = Color.LightGray, modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = "Profile Icon",
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun ProfileImage(url: String?, size: Dp = 32.dp, modifier: Modifier = Modifier) {
    if (!url.isNullOrBlank()) {
        AsyncImage(
            model = url,
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.Gray)
        )
    } else {
        ProfileIcon(size = size, modifier = modifier)
    }
}

@Composable
fun FriendsScreenFunction(viewModel: FriendsViewModel = viewModel(), onStartChat: (ChatPreview) -> Unit) {
    // remembers state of search bar and collects all users of the database for name searching
    var searchText by remember { mutableStateOf("") }
    val allUsers by viewModel.allUsers.collectAsState()

    // remembers state of the friend requests pop-out page and collects all users that cant be friended
    // (due to already being friends or an active request
    var showRequests by rememberSaveable { mutableStateOf(false) }
    val blockedUserIds by viewModel.blockedUserIds.collectAsState()
    val friendUserIds by viewModel.friendUserIds.collectAsState()
    val incomingRequestUserIds by viewModel.incomingRequestUserIds.collectAsState()

    // grab the current users id for use throughout the function
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: return

    // grab the state of selected friends for profile card viewing
    var selectedFriend by remember { mutableStateOf<Pair<String, Map<String, Any>>?>(null) }
    var showFriendSheet by remember { mutableStateOf(false) }

    // grab state of current incoming friend requests
    var friendRequests by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())}

    // grab the state of the friends list to be displayed
    var friendsList by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }

    // logging for user list load
    LaunchedEffect(allUsers) {
        Log.d("FRIENDS", "Fetched ${allUsers.size} users from FireStore?")
    }

    // launch effect to fetch friend requests
    LaunchedEffect(showRequests) {
        Log.d("REQUESTS", "Fetching friend requests for $currentUserId")
        FirebaseFirestore.getInstance()
            .collection("Users").document(currentUserId)
            .collection("FriendRequests")
            .get()
            .addOnSuccessListener { result ->
                Log.d("REQUESTS", "Found ${result.documents.size} request(s)")
                friendRequests = result.documents.mapNotNull { doc ->
                    Log.d("REQUESTS", "Request doc: ${doc.id} = ${doc.data}")
                    val data = doc.data ?: return@mapNotNull null
                    doc.id to data
                }
            }
    }

    // launch effect to fetch friends list
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("Users").document(currentUserId)
            .collection("Friends")
            .get()
            .addOnSuccessListener { result ->
                friendsList = result.documents.mapNotNull { doc ->
                    val friendData = doc.data ?: return@mapNotNull null
                    doc.id to friendData
                }
            }
    }

    // filtered user list
    val filteredUsers = remember(searchText, allUsers) {
        try {
            if (searchText.isBlank()) {
                emptyList()
            } else {
                allUsers.filter {
                    it.second.username?.startsWith(searchText, ignoreCase = true) == true
                }.sortedByDescending { it.second.username?.length ?: 0 }
            }
        } catch (e: Exception) {
            Log.e("FRIENDS", "Filtering crashed", e)
            emptyList()
        }
    }


    // top bar for logo and friends icon
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            // Friends icon
            IconButton(
                onClick = { showRequests = true },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Friend Requests",
                    tint = Color.White
                )
            }

            // Title text
            Text(
                text = "NowPlaying.",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (searchText.isEmpty()) {
                            Text(
                                text = "Add or search friends",
                                color = Color.LightGray,
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // clears the search bar button if its not empty
            if(searchText.isNotBlank()) {
                IconButton(onClick = { searchText = "" },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp))
                {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Search",
                        tint = Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // display of filtered usernames
        filteredUsers.forEach { (uid, user) ->
            val isBlocked = blockedUserIds.contains(uid)
            val isFriend = friendUserIds.contains(uid)
            val hasIncomingRequest = incomingRequestUserIds.contains(uid)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        searchText = user.username ?: ""
                    }
                    .padding(vertical = 8.dp)
            ) {
                // Left-aligned content (icon + username)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {

                    // Profile image for searched users
                    ProfileImage(url = user.profileImageUrl, modifier = Modifier.padding(end = 8.dp))

                    Text(
                        text = user.username ?: "Unknown user",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Right-aligned button
                Button(
                    onClick = {
                        when {
                            isFriend -> {
                                Log.d("FRIENDS", "Open chat with ${user.username}")
                                onStartChat(
                                    ChatPreview(
                                        friendId = uid,
                                        friendName = user.username ?: "Unknown",
                                        profileImageUrl = user.profileImageUrl ?: "",
                                        lastMessage = "",
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }
                            hasIncomingRequest -> {
                                // accept logic to prevent duplicate requests
                                val db = FirebaseFirestore.getInstance()

                                db.collection("Users").document(currentUserId)
                                    .collection("Friends")
                                    .document(uid)
                                    .set(mapOf(
                                        "firstName" to user.firstName,
                                        "username" to user.username
                                    ))
                                    .addOnSuccessListener {
                                        // delete the friend requests from the db
                                        db.collection("Users").document(currentUserId)
                                            .collection("FriendRequests")
                                            .document(uid)
                                            .delete()
                                            .addOnSuccessListener {
                                                Log.d("FRIENDS", "Accepted request from $uid")

                                                // update
                                                viewModel.fetchBlockedAndFriendUsers()
                                            }
                                    }
                            }
                            isBlocked -> {
                                // do nothing
                            }
                            else -> {
                                // send request
                                val db = FirebaseFirestore.getInstance()
                                db.collection("Users").document(currentUserId).get()
                                    .addOnSuccessListener { currentUserDoc ->
                                        val senderUsername = currentUserDoc.getString("username")
                                            ?: return@addOnSuccessListener
                                        val senderFirstName = currentUserDoc.getString("firstName") ?: ""
                                        val senderProfileImageUrl = currentUserDoc.getString("profileImageUrl") ?: ""

                                        db.collection("Users").document(uid)
                                            .collection("FriendRequests")
                                            .document(currentUserId)
                                            .set(
                                                mapOf(
                                                    "firstName" to senderFirstName,
                                                    "username" to senderUsername,
                                                    "profileImageUrl" to senderProfileImageUrl
                                                )
                                            )
                                            .addOnSuccessListener {
                                                Log.d("FRIEND_REQUEST", "Request sent to $uid")
                                                viewModel.fetchBlockedAndFriendUsers()
                                            }
                                            .addOnFailureListener {
                                                Log.e(
                                                    "FRIEND_REQUEST",
                                                    "Failed to send request",
                                                    it
                                                )
                                            }

                                    }
                            }
                        }
                    },
                    enabled = !(isBlocked && !isFriend),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFriend) Color.Gray else if (isBlocked) Color.DarkGray else Color.Gray
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = when {
                            isFriend -> "Chat"
                            hasIncomingRequest -> "Accept"
                            isBlocked -> "Requested"
                            else -> "Add"
                        },
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(40.dp))

        // invite friends link box
        Button(
            onClick = { /* Optional: hook up link sharing */ },
            colors = ButtonDefaults.buttonColors(Color.DarkGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(40.dp),
                    tint = Color.LightGray
                )

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Invite friends on NowPlaying.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )

                    Text(
                        text = "nowplay.al/username",
                        color = Color.Gray,
                        fontSize = 14.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "My Friends",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (friendsList.isEmpty()) {
            Text("No friends yet :(", color = Color.Gray)
        } else {
            friendsList.forEach { (uid, data) ->
                val firstName = data["firstName"] as? String ?: "unknown"
                val username = data["username"] as? String ?: "unknown_user"
                val profileImageUrl = data["profileImageUrl"] as? String ?: ""

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedFriend = uid to data
                            showFriendSheet = true
                        }
                        .padding(vertical = 8.dp)
                ) {
                    // Profile picture for friended users
                    ProfileImage(url = profileImageUrl, modifier = Modifier.padding(end = 8.dp))


                    // Name and Username
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = firstName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "@$username",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    // Chat Button
                    Button(
                        onClick = {
                            val chatId = FirebaseFirestore.getInstance().collection("chats").document().id
                            onStartChat(
                                ChatPreview(
                                    friendId = uid,
                                    friendName = data["username"] as? String ?: "Unknown",
                                    profileImageUrl = data["profileImageUrl"] as? String ?: "",
                                    lastMessage = "",
                                    timestamp = System.currentTimeMillis(),
                                    chatId = chatId
                                )
                            )},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Chat", fontSize = 12.sp, color = Color.White)
                    }
                }
            }

        }

    }

    // friend card popout
    AnimatedVisibility(
        visible = showFriendSheet && selectedFriend != null,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        val (uid, data) = selectedFriend!!
        val profileImageUrl = data["profileImageUrl"] as? String ?: ""
        val bio = data["bio"] as? String ?: ""
        val location = data["location"] as? String ?: ""
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(26, 27, 28), RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Back arrow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showFriendSheet = false }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Profile icon
                if (profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Icon",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = data["firstName"] as? String ?: "Unknown",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "@${data["username"] as? String ?: "user"}",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                if (bio.isNotBlank()) {
                    Text(
                        text = bio,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                if (location.isNotBlank()) {
                    Text(
                        text = location,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val chatId = FirebaseFirestore.getInstance().collection("chats").document().id
                        onStartChat(
                            ChatPreview(
                                friendId = uid,
                                friendName = data["username"] as? String ?: "Unknown",
                                profileImageUrl = data["profileImageUrl"] as? String ?: "",
                                lastMessage = "",
                                timestamp = System.currentTimeMillis(),
                                chatId = chatId
                            )
                        )

                        showFriendSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Message", fontSize = 16.sp, color = Color.White)
                }

                // remove friend button
                TextButton(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("Users").document(currentUserId)
                            .collection("Friends")
                            .document(uid)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("FRIENDS", "Removed $uid from friends")

                                // now remove YOU from the friend's friends list
                                db.collection("Users").document(uid)
                                    .collection("Friends")
                                    .document(currentUserId)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d("FRIENDS", "Removed you from $uid's friends list")
                                    }
                                    .addOnFailureListener {
                                        Log.e("FRIENDS", "Failed to remove you from $uid's friends list", it)
                                    }

                                showFriendSheet = false

                                // now refresh the friend list
                                db.collection("Users").document(currentUserId)
                                    .collection("Friends")
                                    .get()
                                    .addOnSuccessListener { result ->
                                        friendsList = result.documents.mapNotNull { doc ->
                                            val friendData = doc.data ?: return@mapNotNull null
                                            doc.id to friendData
                                        }
                                    }
                            }
                            .addOnFailureListener {
                                Log.e("FRIENDS", "Failed to remove friend", it)
                            }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Remove Friend", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }

    // pop out page for friend requests
    AnimatedVisibility(
        visible = showRequests,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(Color(26, 27, 28), RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Friend Requests", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showRequests = false },
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // if you don't have any friend requests
                if (friendRequests.isEmpty()) {
                    Text("No friend requests :(", color = Color.Gray)
                } else {
                    // you do have friend requests
                    friendRequests.forEach { (senderId, data) ->
                        val firstName = data["firstName"] as? String ?: "unknown"
                        val username = data["username"] as? String ?: "unknown"
                        val profileImageUrl = data["profileImageUrl"] as? String ?: ""

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // The users Icon
                            if (profileImageUrl.isNotBlank()) {
                                ProfileImage(url = profileImageUrl, modifier = Modifier.padding(end = 8.dp))

                            } else {
                                ProfileIcon(modifier = Modifier.padding(end = 8.dp))
                            }


                            // Name and Username
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = firstName,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "@$username",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }

                            // Accept button
                            Button(
                                onClick = {
                                    val db = FirebaseFirestore.getInstance()

                                    // Add sender to the current user's friends list
                                    db.collection("Users").document(currentUserId)
                                        .collection("Friends")
                                        .document(senderId)
                                        .set(mapOf(
                                            "firstName" to data["firstName"],
                                            "username" to data["username"],
                                            "profileImageUrl" to data["profileImageUrl"],
                                            "bio" to data["bio"],
                                            "location" to data["location"]
                                        ))
                                        .addOnSuccessListener {
                                            // Add the current user to the sender's friends list
                                            db.collection("Users").document(currentUserId).get()
                                                .addOnSuccessListener { currentUserDoc ->
                                                    val myUsername = currentUserDoc.getString("username") ?: ""
                                                    val myFirstName = currentUserDoc.getString("firstName") ?: ""
                                                    val myProfileImageUrl = currentUserDoc.getString("profileImageUrl") ?: ""
                                                    val myBio = currentUserDoc.getString("bio") ?: ""
                                                    val myLocation = currentUserDoc.getString("location") ?: ""

                                                    db.collection("Users").document(senderId)
                                                        .collection("Friends")
                                                        .document(currentUserId)
                                                        .set(mapOf(
                                                            "firstName" to myFirstName,
                                                            "username" to myUsername,
                                                            "profileImageUrl" to myProfileImageUrl,
                                                            "bio" to myBio,
                                                            "location" to myLocation
                                                        ))
                                                        .addOnSuccessListener {
                                                            // Now remove the request
                                                            db.collection("Users").document(currentUserId)
                                                                .collection("FriendRequests")
                                                                .document(senderId)
                                                                .delete()
                                                                .addOnSuccessListener {
                                                                    Log.d("FRIEND_REQUEST", "Removed $senderId from friend requests")

                                                                    friendRequests = friendRequests.filterNot { it.first == senderId }

                                                                    db.collection("Users").document(currentUserId)
                                                                        .collection("FriendRequests")
                                                                        .get()
                                                                        .addOnSuccessListener { result ->
                                                                            friendRequests = result.documents.mapNotNull { doc ->
                                                                                val requestData = doc.data ?: return@mapNotNull null
                                                                                doc.id to requestData
                                                                            }
                                                                        }
                                                                }
                                                                .addOnFailureListener {
                                                                    Log.e("FRIEND_REQUEST", "Failed to remove request", it)
                                                                }
                                                        }
                                                        .addOnFailureListener {
                                                            Log.e("FRIEND_REQUEST", "Failed to update sender's friends list", it)
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    Log.e("FRIEND_REQUEST", "Failed to fetch current user info", it)
                                                }
                                        }
                                        .addOnFailureListener {
                                            Log.e("FRIEND_REQUEST", "Failed to add friend to current user's list", it)
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Accept", fontSize = 12.sp, color = Color.White)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Decline button
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Decline",
                                tint = Color.LightGray,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        // remove the request from your friend requests, refresh requests so you can also be added again
                                        val db = FirebaseFirestore.getInstance()
                                        db.collection("Users").document(currentUserId)
                                            .collection("FriendRequests")
                                            .document(senderId)
                                            .delete()
                                            .addOnSuccessListener {
                                                Log.d("FRIEND_REQUEST", "Declined request from $senderId")

                                                // now refresh after deletion
                                                FirebaseFirestore.getInstance()
                                                    .collection("Users")
                                                    .document(currentUserId)
                                                    .collection("FriendRequests")
                                                    .get()
                                                    .addOnSuccessListener { result ->
                                                        friendRequests = result.documents.mapNotNull { doc ->
                                                            val requestData = doc.data ?: return@mapNotNull null
                                                            doc.id to requestData
                                                        }
                                                    }
                                            }
                                            .addOnFailureListener {
                                                Log.e("FRIEND_REQUEST", "Failed to decline request", it)
                                            }
                                    },
                            )

                        }
                    }
                }
            }
        }
    }

}

