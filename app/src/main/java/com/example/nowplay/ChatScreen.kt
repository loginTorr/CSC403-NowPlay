package com.example.nowplay

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Button
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FieldValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*


data class Message(
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val isPost: Boolean = false,
    val postData: PostData? = null
)

data class ChatPreview (
    val friendId: String,
    val friendName: String,
    val profileImageUrl: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val chatId: String = "",
    val isUnread: Boolean = false
)

data class PostData(
    val songName: String,
    val artistName: String,
    val albumName: String,
    val songPicture: String
)

// for formatting timestamps for chat messages
fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val date = Date(timestamp)

    return when {
        diff < 86_400_000 -> { // less than 24 hours
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(date) // e.g. "2:45 PM"
        }
        diff < 172_800_000 -> {
            "Yesterday"
        }
        else -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(date) // e.g. "May 17"
        }
    }
}

@Composable
fun ChatListScreen(
    chats: List<ChatPreview>,
    onOpenChat: (ChatPreview) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(26, 27, 28))
            .padding(16.dp)
    ) {
        Text("Messages", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(chats) { chat ->
                ChatRow(chat = chat, onClick = { onOpenChat(chat) })
            }
        }
    }
}


@Composable
fun ChatRow(chat: ChatPreview, onClick: () -> Unit) {
    val rowColor = if (chat.isUnread) Color(0xFF2D2D2D) else Color.Transparent

    Surface(
        color = rowColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowColor)
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImage(url = chat.profileImageUrl, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(chat.friendName, color = Color.White, fontSize = 16.sp)
                    Text(text = formatTimestamp(chat.timestamp), color = Color.Gray, fontSize = 12.sp)
                }

                Text(chat.lastMessage, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
            }
        }
    }


}

// deletes the chat row once a user deletes a chat
fun deleteChatRowForCurrentUser(friendId: String) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseFirestore.getInstance()
        .collection("Users").document(currentUserId)
        .collection("Chats").document(friendId)
        .delete()
}

// responsible for showing the chat screens, including the back button, deletes, sending messages
@Composable
fun ChatDetailScreen(friend: ChatPreview, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var menuExpanded by remember { mutableStateOf(false) }

    // for live messaging unify the chat Id
    val chatId = remember(friend.friendId) {
        listOf(currentUserId, friend.friendId).sorted().joinToString("_") // new
    }

    // real time listener for shared messages collection
    LaunchedEffect(chatId) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Messages").document(chatId)
            .collection("Messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages.clear()
                    messages.addAll(snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null

                        val isPost = data["isPost"] as? Boolean ?: false
                        var postData: PostData? = null

                        if (isPost) {
                            val postMap = data["postData"] as? Map<String, Any>
                            if (postMap != null) {
                                postData = PostData(
                                    songName = postMap["songName"] as? String ?: "",
                                    artistName = postMap["artistName"] as? String ?: "",
                                    albumName = postMap["albumName"] as? String ?: "",
                                    songPicture = postMap["songPicture"] as? String ?: ""
                                )
                            }
                        }

                        Message(
                            senderId = data["senderId"] as? String ?: return@mapNotNull null,
                            text = data["text"] as? String ?: "",
                            timestamp = data["timestamp"] as? Long ?: 0L,
                            isPost = isPost,
                            postData = postData
                        )
                    })
                }
            }

        db.collection("Users").document(currentUserId)
            .collection("Chats").document(friend.friendId)
            .update("unread", false)
    }


    Column(modifier = Modifier.fillMaxSize()
        .background(Color(26, 27, 28), shape = RoundedCornerShape(0.dp))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(friend.friendName, color = Color.White, fontSize = 20.sp)
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete Chat") },
                        onClick = {
                            menuExpanded = false
                            deleteChatRowForCurrentUser(friend.friendId)
                            onBack() // exit the chat screen
                        }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                val isMe = msg.senderId == currentUserId
                if (msg.isPost && msg.postData != null) {
                    PostMessageBubble(msg, isMe)
                } else {
                    RegularMessageBubble(msg, isMe)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

        }


        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (messageText.isBlank()) {
                    return@Button
                } else {
                    val db = FirebaseFirestore.getInstance()
                    val message = mapOf(
                        "senderId" to currentUserId,
                        "text" to messageText,
                        "timestamp" to System.currentTimeMillis(),
                        "isPost" to false
                    )

                    // Send message to shared chat location
                    db.collection("Messages").document(chatId)
                        .collection("Messages")
                        .add(message)

                    // Update users chat preview (for timestamp + lastMessage only)
                    val myPreviewUpdate = mapOf(
                        "lastMessage" to messageText,
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("Users").document(currentUserId)
                        .collection("Chats").document(friend.friendId)
                        .update(myPreviewUpdate)


                    // Update receiver chat preview (include unread = true)
                    val recipientPreviewUpdate = mapOf(
                        "lastMessage" to messageText,
                        "timestamp" to System.currentTimeMillis(),
                        "unread" to true
                    )

                    db.collection("Users").document(friend.friendId)
                        .collection("Chats").document(currentUserId)
                        .update(recipientPreviewUpdate)

                    messageText = ""
                }
            }) {
                Text("Send")
            }

        }
    }
}


// chat view model to handle logic of chats and list any chats opened
class ChatViewModel : ViewModel() {

    private val _chatList = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chatList: StateFlow<List<ChatPreview>> = _chatList

    fun startListeningForChats() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Users").document(uid)
            .collection("Chats")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val chats = snapshots.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        ChatPreview(
                            friendId = data["friendId"] as? String ?: return@mapNotNull null,
                            friendName = data["friendName"] as? String ?: "Unknown",
                            profileImageUrl = data["profileImageUrl"] as? String ?: "",
                            lastMessage = data["lastMessage"] as? String ?: "",
                            timestamp = data["timestamp"] as? Long ?: 0L,
                            isUnread = data["unread"] as? Boolean ?: false
                        )
                    }.sortedByDescending { it.timestamp }
                    _chatList.value = chats
                }
            }
    }

    fun createChatEntriesForBothUsers(chat: ChatPreview) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val otherUserId = chat.friendId
        val chatId = chat.chatId

        val currentUserChat = mapOf(
            "chatId" to chatId,
            "friendId" to otherUserId,
            "friendName" to chat.friendName,
            "profileImageUrl" to chat.profileImageUrl,
            "lastMessage" to "",
            "timestamp" to FieldValue.serverTimestamp()
        )

        val currentUserRef = db.collection("Users").document(currentUserId)
        val friendRef = db.collection("Users").document(otherUserId)

        currentUserRef.get().addOnSuccessListener { currentUserDoc ->
            val myName = currentUserDoc.getString("username") ?: "Unknown"
            val myImage = currentUserDoc.getString("profileImageUrl") ?: ""

            val friendChat = mapOf(
                "chatId" to chatId,
                "friendId" to currentUserId,
                "friendName" to myName,
                "profileImageUrl" to myImage,
                "lastMessage" to "",
                "timestamp" to FieldValue.serverTimestamp()
            )

            currentUserRef.collection("Chats").document(otherUserId).set(currentUserChat)
            friendRef.collection("Chats").document(currentUserId).set(friendChat)
                .addOnFailureListener{
                    Log.e("FIRESTORE", "Error creating chat entries", it)
                }
        }
    }

}

@Composable
fun RegularMessageBubble(msg: Message, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isMe) Color.LightGray else Color.DarkGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            Column {
                Text(
                    text = msg.text,
                    color = if (isMe) Color.Black else Color.White,
                    fontSize = 16.sp
                )

                Text(
                    text = formatTimestamp(msg.timestamp),
                    color = if (isMe) Color.DarkGray else Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun PostMessageBubble(msg: Message, isMe: Boolean) {
    val postData = msg.postData ?: return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isMe) Color(200, 200, 200) else Color(70, 70, 70),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Message text
                if (msg.text.isNotEmpty()) {
                    Text(
                        text = msg.text,
                        color = if (isMe) Color.Black else Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Song card with album art
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(40, 40, 40),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Album art
                        AsyncImage(
                            model = postData.songPicture,
                            contentDescription = "Album Cover",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Song details
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = postData.songName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = postData.artistName,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = postData.albumName,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Timestamp
                Text(
                    text = formatTimestamp(msg.timestamp),
                    color = if (isMe) Color.DarkGray else Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}