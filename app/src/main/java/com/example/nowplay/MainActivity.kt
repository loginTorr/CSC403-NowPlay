package com.example.nowplay

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.nowplay.ui.theme.NowPlayTheme
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Intent
import android.net.Uri
import android.widget.Toast

data class BottomNavigationItem(
    val screen: Any,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null,
    val showLabel: Boolean
)

// database user class
data class User(
    var username: String? = null,
    val phoneNumber: String? = null,
    val firstName: String? = null,
    val birthday: String? = null,
    var bio: String? = null,
    var location: String? = null
)

class MainActivity : ComponentActivity() {
    private lateinit var spotifyAuthManager: SpotifyAuthManager
    private val accessToken = mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // had to do this for some reason

        enableEdgeToEdge()
        val user = FirebaseAuth.getInstance().currentUser
        val startDestination: Any = if (user != null) HomeScreen else LoginSignupScreen
        spotifyAuthManager = SpotifyAuthManager(this)

        intent?.data
            ?.takeIf { it.toString().startsWith("nowplay://callback") }
            ?.let {
                handleSpotifyRedirect(intent)
            }
        getSharedPreferences("spotify_auth", MODE_PRIVATE)
            .getString("access_token", null)
            ?.let { token ->
                accessToken.value = token
            }

        setContent {

            val usernameState = remember { mutableStateOf("Loading...") }

            LaunchedEffect(Unit) {
                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()

                val listener = FirebaseAuth.AuthStateListener { _ ->
                    if (user != null) {
                        val uid = user.uid
                        db.collection("Users").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val username = document.getString("username")
                                    usernameState.value = username ?: "No username found"
                                } else {
                                    usernameState.value = "No username found"
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w("FIRESTORE", "Error getting username", exception)
                                usernameState.value = "Error loading username"
                            }
                    } else {
                        usernameState.value = "No user logged in"
                    }
                }

                auth.addAuthStateListener(listener)
            }





            NowPlayTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val onboardingScreens = listOf(
                    FirstNameScreen::class.qualifiedName,
                    BirthdayScreen::class.qualifiedName,
                    PhoneNumberScreen::class.qualifiedName,
                    UsernameScreen::class.qualifiedName,
                    LoginScreen::class.qualifiedName,
                    LoginSignupScreen::class.qualifiedName,
                    ViewPostScreen::class.qualifiedName,
                    SettingsScreen::class.qualifiedName,
                    EditProfileScreen::class.qualifiedName
                )

                val showBottomBar = currentDestination?.route !in onboardingScreens


                val firstName = rememberSaveable { mutableStateOf("") }
                val birthday = rememberSaveable { mutableStateOf("") }
                val phoneNumber = rememberSaveable { mutableStateOf("") }
                val username = rememberSaveable { mutableStateOf("") }

                val items = listOf(
                    BottomNavigationItem(
                        screen = HomeScreen,
                        title = "Home",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false,
                        showLabel = true
                    ),
                    BottomNavigationItem(
                        screen = FriendsScreen,
                        title = "Friends",
                        selectedIcon = Icons.Filled.Favorite,
                        unselectedIcon = Icons.Outlined.FavoriteBorder,
                        hasNews = false,
                        showLabel = true
                    ),
                    BottomNavigationItem(
                        screen = PostScreen,
                        title = "Post",
                        selectedIcon = Icons.Filled.AddCircle,
                        unselectedIcon = Icons.Outlined.AddCircle,
                        hasNews = false,
                        showLabel = false
                    ),
                    BottomNavigationItem(
                        screen = ChatScreen,
                        title = "Chat",
                        selectedIcon = Icons.Filled.Email,
                        unselectedIcon = Icons.Outlined.Email,
                        hasNews = false,
                        badgeCount = null,
                        showLabel = true
                    ),
                    BottomNavigationItem(
                        screen = ProfileScreen,
                        title = "Profile",
                        selectedIcon = Icons.Filled.AccountCircle,
                        unselectedIcon = Icons.Outlined.AccountCircle,
                        hasNews = false,
                        showLabel = true
                    )
                )

                var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        containerColor = Color(26, 27, 28),
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar(
                                    contentColor = Color.White,
                                    containerColor = Color.DarkGray
                                ) {
                                    items.forEachIndexed { index, item ->
                                        NavigationBarItem(
                                            selected = selectedItemIndex == index,
                                            onClick = {
                                                selectedItemIndex = index
                                                navController.navigate(item.screen)
                                            },
                                            label = {
                                                if (item.showLabel) {
                                                    Text(text = item.title, color = Color.White)
                                                }
                                            },
                                            icon = {
                                                BadgedBox(
                                                    badge = {
                                                        if (item.badgeCount != null) {
                                                            Badge {
                                                                Text(text = item.badgeCount.toString())
                                                            }
                                                        } else if (item.hasNews) {
                                                            Badge()
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (index == selectedItemIndex) {
                                                            item.selectedIcon
                                                        } else item.unselectedIcon,
                                                        contentDescription = item.title,
                                                        tint = if (index == selectedItemIndex) {
                                                            Color.DarkGray
                                                        } else Color.White
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable<LoginSignupScreen> {
                                LoginSignupScreenFunction(navController = navController)
                            }

                            composable<LoginScreen> {
                                LoginScreenFunction(navController = navController)
                            }

                            composable<FirstNameScreen> {
                                FirstNameScreenFunction(firstName = firstName, navController = navController)
                            }


                            composable<BirthdayScreen> {
                                BirthdayScreenFunction(birthday = birthday, navController = navController)
                            }


                            composable<PhoneNumberScreen> {
                                PhoneNumberScreenFunction(phoneNumber = phoneNumber, navController = navController)
                            }


                            composable<UsernameScreen> {
                                UsernameScreenFunction(
                                    firstName = firstName,
                                    birthday = birthday,
                                    phoneNumber = phoneNumber,
                                    username = username,
                                    navController = navController
                                )
                            }

                            composable<HomeScreen> {
                                HomeScreenFunction()
                            }
                            composable<FriendsScreen> {
                                FriendsScreenFunction(viewModel())
                            }
                            composable<PostScreen> {
                                PostScreenFunction(
                                    spotifyAuthManager = spotifyAuthManager,
                                    accessToken = accessToken
                                )
                            }
                            composable<ChatScreen> {
                                ChatScreenFunction()
                            }
                            composable<ProfileScreen> {
                                ProfileScreenFunction(navController = navController)
                            }
                            composable<ViewPostScreen> {
                                ViewPostScreenFunction(navController = navController)
                            }
                            composable<SettingsScreen> {
                                SettingsScreenFunction(navController = navController)
                            }
                            composable<EditProfileScreen> {
                                EditProfileScreenFunction(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data
            ?.takeIf { it.toString().startsWith("nowplay://callback") }
            ?.let {
                handleSpotifyRedirect(intent)   // ← again, pass the Intent
            }
    }

    private fun handleSpotifyRedirect(intent: Intent) {
        Log.d("SPOTIFY", "Redirect Intent received: ${intent.data}")
        spotifyAuthManager.handleRedirectIntent(intent) { success, token ->
            if (success && token != null) {
                accessToken.value = token
                // Persist the token
                getSharedPreferences("spotify_auth", MODE_PRIVATE)
                    .edit()
                    .putString("access_token", token)
                    .apply()
                Log.d("SPOTIFY", "Token successfully stored: $token")
            } else {
                Log.e("SPOTIFY", "Token exchange failed")
            }
        }
    }
}

// For fetching the users from the database and maintaining them
// Throughout navigation, allows it so the users only have to be
// Fetched once instead of everytime you use the search bar
// NEEDED for friends screen to work atm
class FriendsViewModel : ViewModel() {
    private val _allUsers = MutableStateFlow<List<Pair<String, User>>>(emptyList()) // UID and user
    val allUsers: StateFlow<List<Pair<String, User>>> = _allUsers

    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds

    private val _friendUserIds = MutableStateFlow<Set<String>>(emptySet())
    val friendUserIds: StateFlow<Set<String>> = _friendUserIds

    private val _incomingRequestUserIds = MutableStateFlow<Set<String>>(emptySet())
    val incomingRequestUserIds: StateFlow<Set<String>> = _incomingRequestUserIds

    init {
        fetchUsers()
        fetchBlockedAndFriendUsers()
    }

    private fun fetchUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener { result ->
            _allUsers.value = result.documents.mapNotNull { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null && doc.id != currentUserId) {
                    doc.id to user
                } else {
                    null
                }
            }
        }
    }

    // gets the users that you sent friend requests to which will be blocked
    fun fetchBlockedAndFriendUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val blockedIds = mutableSetOf<String>()
        val friendIds = mutableSetOf<String>()
        val incomingRequestIds = mutableSetOf<String>()

        db.collection("Users").get().addOnSuccessListener { users ->
            var pendingCount = users.size()
            if (pendingCount == 0) {
                finalizeBlockedUsers(blockedIds, friendIds, incomingRequestIds, currentUserId)
                return@addOnSuccessListener
            }

            users.documents.forEach { doc ->
                val otherUserId = doc.id
                db.collection("Users").document(otherUserId)
                    .collection("FriendRequests")
                    .document(currentUserId)
                    .get()
                    .addOnSuccessListener { requestDoc ->
                        if (requestDoc.exists()) {
                            blockedIds.add(otherUserId)
                        }
                        pendingCount--
                        if (pendingCount == 0) {
                            finalizeBlockedUsers(blockedIds, friendIds, incomingRequestIds, currentUserId)
                        }
                    }
                    .addOnFailureListener {
                        Log.e("BLOCKED_USERS", "Error checking request for $otherUserId", it)
                        pendingCount--
                        if (pendingCount == 0) {
                            finalizeBlockedUsers(blockedIds, friendIds, incomingRequestIds, currentUserId)
                        }
                    }
            }
        }.addOnFailureListener {
            Log.e("BLOCKED_USERS", "Failed to fetch users", it)
        }
    }

    // also add the users you are ALREADY friends with to the blocked list
    private fun finalizeBlockedUsers(blockedIds: MutableSet<String>, friendIds: MutableSet<String>, incomingRequestIds: MutableSet<String>, currentUserId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(currentUserId)
            .collection("Friends")
            .get()
            .addOnSuccessListener { friendsSnapshot ->
                friendsSnapshot.documents.forEach { doc ->
                    friendIds.add(doc.id)
                    blockedIds.add(doc.id)
                }

                // check who sent you a request (incoming)
                db.collection("Users").document(currentUserId)
                    .collection("FriendRequests")
                    .get()
                    .addOnSuccessListener { requestsSnapshot ->
                        requestsSnapshot.documents.forEach { doc ->
                            incomingRequestIds.add(doc.id)
                        }


                        // lists out the users that you CANNOT send another request to
                        Log.d("BLOCKED_USERS", "Final blocked list: $blockedIds")
                        _blockedUserIds.value = blockedIds.toSet()

                        // lists out the users that you are already friends with
                        Log.d("BLOCKED_USERS", "Final friend list: $friendIds")
                        _friendUserIds.value = friendIds.toSet()

                        // lists out the users that sent you a request
                        Log.d("BLOCKED_USERS", "Final incoming request list: $incomingRequestIds")
                        _incomingRequestUserIds.value = incomingRequestIds.toSet()
                    }
                    .addOnFailureListener {
                        Log.e("BLOCKED_USERS", "Failed to fetch incoming requests", it)
                    }
            }
            .addOnFailureListener { // error checking if failed to fetch friends or requests
                Log.e("BLOCKED_USERS", "Failed to fetch friends", it)
            }
    }


}








