package com.example.nowplay

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                val items = listOf(
                    BottomNavigationItem(
                        screen = HomeScreen,
                        title = "Home",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false,
                        showLabel = true,
                    ),
                    BottomNavigationItem(
                        screen = FriendsScreen,
                        title = "Friends",
                        selectedIcon = Icons.Filled.Favorite,
                        unselectedIcon = Icons.Outlined.FavoriteBorder,
                        hasNews = false,
                        showLabel = true,
                    ),
                    BottomNavigationItem(
                        screen = PostScreen,
                        title = "Post",
                        selectedIcon = Icons.Filled.AddCircle,
                        unselectedIcon = Icons.Outlined.AddCircle,
                        hasNews = false,
                        showLabel = false,
                    ),
                    BottomNavigationItem(
                        screen = ChatScreen,
                        title = "Chat",
                        selectedIcon = Icons.Filled.Email,
                        unselectedIcon = Icons.Outlined.Email,
                        hasNews = false,
                        badgeCount = null,
                        showLabel = true,
                    ),
                    BottomNavigationItem(
                        screen = ProfileScreen,
                        title = "Profile",
                        selectedIcon = Icons.Filled.AccountCircle,
                        unselectedIcon = Icons.Outlined.AccountCircle,
                        hasNews = false,
                        showLabel = true,
                    ),
                )
                    var selectedItemIndex by rememberSaveable {
                    mutableStateOf(0)
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Scaffold(
                        containerColor = Color(26,27,28),
                        bottomBar = {
                            NavigationBar(
                                contentColor = Color.White,
                                containerColor = Color.DarkGray,
                            ) {
                                items.forEachIndexed { index, item ->
                                    NavigationBarItem(
                                        selected = selectedItemIndex == index,
                                        onClick = {
                                            selectedItemIndex = index
                                            /* TO DO (Tommy): Navigation Controller still needs
                                             to be created in order to create the actual individual
                                             tab pages */
                                            navController.navigate(item.screen)
                                        },
                                        label = {
                                            if (item.showLabel) {
                                                Text(
                                                    text = item.title,
                                                    color = Color.White
                                                )
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
                    ){
                        NavHost(
                            navController = navController,
                            startDestination = HomeScreen,

                            ) {
                            composable<HomeScreen> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center

                                ) {
                                    Text(
                                        text = "Home Screen",
                                        color = Color.White
                                    )
                                }
                            }
                            composable<FriendsScreen> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center

                                ) {
                                    Text(
                                        text = "Friends Screen",
                                        color = Color.White
                                    )
                                }
                            }
                            composable<PostScreen> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center

                                ) {
                                    Text(
                                        text = "Post Screen",
                                        color = Color.White
                                    )
                                }
                            }
                            composable<ChatScreen> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center

                                ) {
                                    Text(
                                        text = "Chat Screen",
                                        color = Color.White
                                    )
                                }
                            }
                            composable<ProfileScreen> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center

                                ) {
                                    Text(
                                        text = "Profile Screen",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Serializable
object HomeScreen

@Serializable
object FriendsScreen

@Serializable
object PostScreen

@Serializable
object ChatScreen

@Serializable
object ProfileScreen