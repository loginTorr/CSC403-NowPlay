package com.example.nowplay

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.nowplay.ui.theme.NowPlayTheme
import kotlinx.serialization.Serializable

data class BottomNavigationItem(
    val screen: Any,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null,
    val showLabel: Boolean
)

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NowPlayTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val onboardingScreens = listOf(
                    FirstNameScreen::class.qualifiedName,
                    BirthdayScreen::class.qualifiedName
                )

                val showBottomBar = currentDestination?.route !in onboardingScreens

                // Onboarding form state
                val firstName = rememberSaveable { mutableStateOf("") }
                val birthday = rememberSaveable { mutableStateOf("") }

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

                var selectedItemIndex by rememberSaveable { mutableStateOf(0) }

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
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = FirstNameScreen
                        ) {
                            // Onboarding: First Name
                            composable<FirstNameScreen> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Enter First Name", color = Color.White)
                                    OutlinedTextField(
                                        value = firstName.value,
                                        onValueChange = { firstName.value = it },
                                        label = { Text("First Name") }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        navController.navigate(BirthdayScreen)
                                    }) {
                                        Text("Next")
                                    }
                                }
                            }

                            // Onboarding: Birthday
                            composable<BirthdayScreen> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Enter Birthday", color = Color.White)
                                    OutlinedTextField(
                                        value = birthday.value,
                                        onValueChange = { birthday.value = it },
                                        label = { Text("Birthday") }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        navController.navigate(HomeScreen) {
                                            popUpTo(FirstNameScreen) { inclusive = true }
                                        }
                                    }) {
                                        Text("Finish")
                                    }
                                }
                            }

                            // Main App Screens
                            composable<HomeScreen> { TextScreen("Home Screen") }
                            composable<FriendsScreen> { TextScreen("Friends Screen") }
                            composable<PostScreen> { TextScreen("Post Screen") }
                            composable<ChatScreen> { TextScreen("Chat Screen") }
                            composable<ProfileScreen> { TextScreen("Profile Screen") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextScreen(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text, color = Color.White)
    }
}

@Serializable object FirstNameScreen
@Serializable object BirthdayScreen
@Serializable object HomeScreen
@Serializable object FriendsScreen
@Serializable object PostScreen
@Serializable object ChatScreen
@Serializable object ProfileScreen