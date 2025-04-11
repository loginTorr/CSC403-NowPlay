package com.example.nowplay

import android.annotation.SuppressLint
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.nowplay.ui.theme.NowPlayTheme
import kotlinx.serialization.Serializable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable

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
    val username: String? = null,
    val phoneNumber: Long? = null
)

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    // database reference to call the databse
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // had to do this for some reason

        // reference to the database
        database = Firebase.database.reference

        enableEdgeToEdge()

        setContent {

            // this state will hold the username, loading until it's fetched
            val usernameState = remember { mutableStateOf("Loading...") }

            // get user reference from firebase
            val userRef = database.child("Users").child("Dickalos")

            // fetch user data
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    usernameState.value = user?.username ?: "No username"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("FIREBASE", "loadUser:onCancelled", error.toException())
                    usernameState.value = "Error"
                }
            })

            NowPlayTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val onboardingScreens = listOf(
                    FirstNameScreen::class.qualifiedName,
                    BirthdayScreen::class.qualifiedName,
                    PhoneNumberScreen::class.qualifiedName,
                    UsernameScreen::class.qualifiedName
                )

                val showBottomBar = currentDestination?.route !in onboardingScreens

                // Onboarding form state
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
                            startDestination = FirstNameScreen,
                            modifier = Modifier.padding(innerPadding)
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
                                        label = { Text("First Name") },
                                        textStyle = TextStyle(color = Color.White) // Set text color to white
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { navController.navigate(BirthdayScreen) },
                                        enabled = firstName.value.isNotBlank()
                                    ) {
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
                                        label = { Text("Birthday") },
                                        textStyle = TextStyle(color = Color.White) // Set text color to white
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { navController.navigate(PhoneNumberScreen) },
                                        enabled = birthday.value.isNotBlank()
                                    ) {
                                        Text("Next")
                                    }

                                }
                            }

                            // Onboarding: Phone Number
                            composable<PhoneNumberScreen> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Enter Phone Number", color = Color.White)
                                    OutlinedTextField(
                                        value = phoneNumber.value,
                                        onValueChange = { phoneNumber.value = it },
                                        label = { Text("Phone Number") },
                                        textStyle = TextStyle(color = Color.White) // Set text color to white
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { navController.navigate(UsernameScreen) },
                                        enabled = phoneNumber.value.isNotBlank()
                                    ) {
                                        Text("Next")
                                    }
                                }
                            }

                            // Onboarding: Username
                            composable<UsernameScreen> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Enter Username", color = Color.White)
                                    OutlinedTextField(
                                        value = username.value,
                                        onValueChange = { username.value = it },
                                        label = { Text("Username") },
                                        textStyle = TextStyle(color = Color.White) // Set text color to white
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            navController.navigate(HomeScreen) {
                                                popUpTo(FirstNameScreen) { inclusive = true }
                                            }
                                        },
                                        enabled = username.value.isNotBlank()
                                    ) {
                                        Text("Finish")
                                    }

                                }
                            }
                            composable<HomeScreen> {
                                HomeScreenFunction()
                            }
                            composable<FriendsScreen> {
                                FriendsScreenFunction()
                            }
                            composable<PostScreen> {
                                PostScreenFunction()
                            }
                            composable<ChatScreen> {
                                ChatScreenFunction()
                            }
                            composable<ProfileScreen> {
                                ProfileScreenFunction(username = usernameState.value)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Serializable object FirstNameScreen
@Serializable object BirthdayScreen
@Serializable object PhoneNumberScreen
@Serializable object UsernameScreen
@Serializable object HomeScreen
@Serializable object FriendsScreen
@Serializable object PostScreen
@Serializable object ChatScreen
@Serializable object ProfileScreen

@Composable
fun HomeScreenFunction() {
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

@Composable
fun FriendsScreenFunction() {
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

@Composable
fun PostScreenFunction() {
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

@Composable
fun ChatScreenFunction() {
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

@Composable
fun ProfileScreenFunction(username: String) {
    val postImages = listOf(R.drawable.image1)
    var showSettings by rememberSaveable { mutableStateOf(false) } // false to hide settings off the rip

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Button(
                    onClick = { showSettings = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon (
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(100.dp),
                tint = Color.LightGray
            )

            // should display the username of Dickalos
            Text(
                username,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 30.sp
            )

            Row(
                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(80.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(postImages.size.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("NowPlays", color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("0", color = Color.White)
                    Text("Friends", color = Color.Gray)
                }
            }

            Button(
                onClick = {},
                content = { Text("Share Profile", color = Color.White) },
                colors = ButtonDefaults.buttonColors(Color.Gray),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(postImages.size) { index ->
                    Image(
                        painter = painterResource(id = postImages[index]),
                        contentDescription = "Post ${index + 1}",
                        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // for the sliding settings page
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .align(Alignment.CenterEnd)
                    .background(Color.DarkGray, RoundedCornerShape(topStart = 48.dp, bottomStart = 16.dp))
                    .padding(16.dp)
            ) {
                Column (
                    modifier = Modifier.fillMaxSize(),
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Settings", color = Color.White, fontSize = 35.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(24.dp).clickable { showSettings = false }, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Option 1", color = Color.White)
                    Text("Option 2", color = Color.White)
                    HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                    Button(
                        onClick = {  },
                        content = { Text("Logout", color = Color.White, fontSize = 20.sp) },
                        colors = ButtonDefaults.buttonColors(Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
