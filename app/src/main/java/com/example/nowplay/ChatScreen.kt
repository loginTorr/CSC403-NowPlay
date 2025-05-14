package com.example.nowplay

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
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class Message(
    val senderId: String,
    val text: String,
    val timestamp: Long
)

data class ChatPreview (
    val friendId: String,
    val friendName: String,
    val profileImageUrl: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L
)

@Composable
fun ChatListScreen (
    chats: List<ChatPreview>,
    onOpenChat: (ChatPreview) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(26, 27, 28))
        .padding(16.dp)) {

        Text("Messages", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn{
            items(chats) { chat ->
                ChatRow(chat = chat, onClick = { onOpenChat(chat) })
            }
        }
    }
}

@Composable
fun ChatRow(chat: ChatPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(url = chat.profileImageUrl, size = 48.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(chat.friendName, color = Color.White, fontSize = 16.sp)
            Text(chat.lastMessage, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun ChatDetailScreen(friend: ChatPreview, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    Column(modifier = Modifier.fillMaxSize()
        .background(Color(26, 27, 28), shape = RoundedCornerShape(0.dp))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(friend.friendName, color = Color.White, fontSize = 20.sp)
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                val isMe = msg.senderId == currentUserId
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
                        Text(
                            text = msg.text,
                            color = if (isMe) Color.Black else Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
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
                placeholder = { Text("Type a message...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (messageText.isNotBlank()) {
                    messages.add(
                        Message(
                            senderId = currentUserId,
                            text = messageText,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    messageText = ""
                }
            }) {
                Text("Send")
            }

        }
    }
}


// chat view model to handle logic of chats
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
                            timestamp = data["timestamp"] as? Long ?: 0L
                        )
                    }.sortedByDescending { it.timestamp }
                    _chatList.value = chats
                }
            }
    }

    fun createChatEntriesForBothUsers(chat: ChatPreview) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentUserRef = db.collection("Users").document(currentUserId)
        val friendRef = db.collection("Users").document(chat.friendId)

        val currentUserChat = mapOf(
            "friendId" to chat.friendId,
            "friendName" to chat.friendName,
            "profileImageUrl" to chat.profileImageUrl,
            "lastMessage" to "",
            "timestamp" to System.currentTimeMillis()
        )

        currentUserRef.get().addOnSuccessListener { currentUserDoc ->
            val myName = currentUserDoc.getString("username") ?: "Unknown"
            val myImage = currentUserDoc.getString("profileImageUrl") ?: ""

            val friendChat = mapOf(
                "friendId" to currentUserId,
                "friendName" to myName,
                "profileImageUrl" to myImage,
                "lastMessage" to "",
                "timestamp" to System.currentTimeMillis()
            )

            currentUserRef.collection("Chats").document(chat.friendId).set(currentUserChat)
            friendRef.collection("Chats").document(currentUserId).get().addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    friendRef.collection("Chats").document(currentUserId).set(friendChat)
                }
            }
        }
    }
}