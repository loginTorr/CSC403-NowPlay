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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


@Composable
fun HomeScreenFunction() {
    var userPost by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
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
                        val friendProfileDoc = db.collection("Users")
                            .document(friendId)
                            .get()
                            .await()

                        val friendUsername = friendProfileDoc.getString("username") ?:
                        friendProfileDoc.getString("displayName") ?:
                        "Friend"

                        val friendPostDoc = db.collection("Users")
                            .document(friendId)
                            .collection("CurrentPost")
                            .document("current")
                            .get()
                            .await()

                        if (friendPostDoc.exists()) {
                            val friendPost = friendPostDoc.toObject(Post::class.java)
                            if (friendPost != null) {
                                val postWithUsername = friendPost.copy(
                                    userId = friendUsername  // Replace the ID with username
                                )
                                tempFriendPosts.add(postWithUsername)
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
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
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
                modifier = Modifier
                    .fillMaxSize()
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
                            .background(
                                Color.Gray.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(10.dp)
                            )
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
            SendButton(post)

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


@Composable
fun SendButton(post: Post? = null) {
    var showDialog by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // The SendButton itself
    IconButton(
        onClick = { showDialog = true }
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send Message",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }

    // Show dialog when button is clicked
    if (showDialog) {
        SendMessageDialog(
            post = post,
            onDismiss = { showDialog = false },
            onSend = { message, selectedFriendId, selectedFriendName ->
                // Send message and post to the chat collection
                if (post != null && selectedFriendId.isNotEmpty()) {
                    sendPostToChat(
                        currentUserId = currentUserId,
                        friendId = selectedFriendId,
                        friendName = selectedFriendName,
                        message = message,
                        post = post
                    )
                }
                showDialog = false
            }
        )
    }
}

// Function to send a post to a chat
private fun sendPostToChat(
    currentUserId: String,
    friendId: String,
    friendName: String,
    message: String,
    post: Post
) {
    val db = Firebase.firestore

    // Get the chat ID (consistent for both users)
    val chatId = listOf(currentUserId, friendId).sorted().joinToString("_")

    // Create post data object
    val postData = mapOf(
        "songName" to post.songName,
        "artistName" to post.artistName,
        "albumName" to post.albumName,
        "songPicture" to post.songPicture
    )

    // Create the message with post data
    val messageData = mapOf(
        "senderId" to currentUserId,
        "text" to message,
        "timestamp" to System.currentTimeMillis(),
        "isPost" to true,
        "postData" to postData
    )

    // Send message to shared chat location
    db.collection("Messages").document(chatId)
        .collection("Messages")
        .add(messageData)
        .addOnSuccessListener {
            Log.d("CHAT", "Post message sent successfully")

            // Update or create chat previews for both users
            updateChatPreview(currentUserId, friendId, friendName, message)

            // Get current user data to update recipient's chat preview
            db.collection("Users").document(currentUserId)
                .get()
                .addOnSuccessListener { currentUserDoc ->
                    val myName = currentUserDoc.getString("username") ?: "Unknown"
                    val myImage = currentUserDoc.getString("profileImageUrl") ?: ""

                    updateChatPreview(friendId, currentUserId, myName, message, true, myImage)
                }
        }
        .addOnFailureListener { e ->
            Log.e("CHAT", "Error sending post message", e)
        }
}

// Helper function to update or create chat preview
private fun updateChatPreview(
    userId: String,
    friendId: String,
    friendName: String,
    lastMessage: String,
    isUnread: Boolean = false,
    friendImage: String = ""
) {
    val db = Firebase.firestore

    // Check if chat exists
    db.collection("Users").document(userId)
        .collection("Chats").document(friendId)
        .get()
        .addOnSuccessListener { document ->
            val previewUpdate = mapOf(
                "lastMessage" to lastMessage,
                "timestamp" to System.currentTimeMillis(),
                "unread" to isUnread
            )

            if (document.exists()) {
                // Update existing chat preview
                db.collection("Users").document(userId)
                    .collection("Chats").document(friendId)
                    .update(previewUpdate)
            } else {
                // Create new chat preview
                val newChatPreview = hashMapOf(
                    "chatId" to listOf(userId, friendId).sorted().joinToString("_"),
                    "friendId" to friendId,
                    "friendName" to friendName,
                    "profileImageUrl" to friendImage,
                    "lastMessage" to lastMessage,
                    "timestamp" to System.currentTimeMillis(),
                    "unread" to isUnread
                )

                db.collection("Users").document(userId)
                    .collection("Chats").document(friendId)
                    .set(newChatPreview)
            }
        }
}

@Composable
fun SendMessageDialog(
    post: Post?,
    onDismiss: () -> Unit,
    onSend: (String, String, String) -> Unit // message, friendId, friendName
) {
    var messageText by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) } // id, name
    var selectedFriendId by remember { mutableStateOf("") }
    var selectedFriendName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch friends list on dialog open
    LaunchedEffect(Unit) {
        isLoading = true
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val db = Firebase.firestore

        db.collection("Users").document(currentUserId)
            .collection("Friends")
            .get()
            .addOnSuccessListener { snapshot ->
                val friendsList = mutableListOf<Pair<String, String>>()

                if (snapshot.documents.isEmpty()) {
                    isLoading = false
                    return@addOnSuccessListener
                }

                var pendingRequests = snapshot.documents.size

                for (doc in snapshot.documents) {
                    val friendId = doc.id
                    db.collection("Users").document(friendId)
                        .get()
                        .addOnSuccessListener { friendDoc ->
                            val friendName = friendDoc.getString("username") ?: "Unknown"
                            friendsList.add(friendId to friendName)

                            pendingRequests--
                            if (pendingRequests == 0) {
                                friends = friendsList
                                isLoading = false
                            }
                        }
                        .addOnFailureListener {
                            pendingRequests--
                            if (pendingRequests == 0) {
                                friends = friendsList
                                isLoading = false
                            }
                        }
                }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(40, 40, 40)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share this song",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Display the post image if available
                post?.songPicture?.let { imageUrl ->
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Song Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Song info
                    Text(
                        text = post.songName,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = post.artistName,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = post.albumName,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Friends dropdown
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                } else if (friends.isEmpty()) {
                    Text(
                        text = "Add friends to share music with them",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    val displayText = if (selectedFriendId.isEmpty()) "Select a friend" else selectedFriendName

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(displayText)
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            friends.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedFriendId = id
                                        selectedFriendName = name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Message input field
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Add a message", color = Color.LightGray) },
                    placeholder = { Text("Check out this song!", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(60, 60, 60),
                        unfocusedContainerColor = Color(50, 50, 50),
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val message = messageText.ifEmpty { "Check out this song!" }
                            onSend(message, selectedFriendId, selectedFriendName)
                        },
                        enabled = selectedFriendId.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1DB954)
                        )
                    ) {
                        Text("Share")
                    }
                }
            }
        }
    }
}

