package com.example.nowplay

import android.annotation.SuppressLint
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
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
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.firestore
import java.util.concurrent.TimeUnit
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults






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
        val user = FirebaseAuth.getInstance().currentUser
        val startDestination = if (user != null) HomeScreen else LoginSignupScreen
        setContent {

            val usernameState = remember { mutableStateOf("Loading...") }
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()

                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
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
                    LoginSignupScreen::class.qualifiedName
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

@Serializable object LoginSignupScreen
@Serializable object LoginScreen
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
fun LoginSignupScreenFunction(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(26, 27, 28)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NowPlaying",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 80.dp)
        )

        Button(
            onClick = { navController.navigate(LoginScreen) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 8.dp)
                .height(50.dp)
        ) {
            Text("Login", fontSize = 18.sp, color = Color.White)
        }

        Button(
            onClick = { navController.navigate(FirstNameScreen) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 8.dp)
                .height(50.dp)
        ) {
            Text("Sign Up", fontSize = 18.sp, color = Color.White)
        }
    }
}


@Composable
fun LoginScreenFunction(navController: NavController) {
    val phoneNumber = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val verificationId = remember { mutableStateOf<String?>(null) }
    val smsCode = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)

        OutlinedTextField(
            value = phoneNumber.value,
            onValueChange = { phoneNumber.value = it },
            label = { Text("Phone Number") },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Username") },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        if (verificationId.value != null) {
            OutlinedTextField(
                value = smsCode.value,
                onValueChange = { smsCode.value = it },
                label = { Text("Verification Code") },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }

        errorMessage.value?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (verificationId.value == null) {
                    // send code
                    val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber(phoneNumber.value)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(context as Activity)
                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                // auto-verification
                                FirebaseAuth.getInstance().signInWithCredential(credential)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            checkUserInFirestore(navController, username.value)
                                        }
                                    }
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                errorMessage.value = "Verification failed: ${e.message}"
                            }

                            override fun onCodeSent(vid: String, token: PhoneAuthProvider.ForceResendingToken) {
                                verificationId.value = vid
                            }

                        })
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                } else {
                    // verify code manually
                    val credential = PhoneAuthProvider.getCredential(verificationId.value!!, smsCode.value)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                checkUserInFirestore(navController, username.value)
                            } else {
                                errorMessage.value = "Sign in failed: ${it.exception?.message}"
                            }
                        }
                }
            },
            enabled = phoneNumber.value.isNotBlank() && username.value.isNotBlank() &&
                    (verificationId.value == null || smsCode.value.length == 6),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (verificationId.value == null) "Send Code" else "Verify & Login")
        }

        TextButton(onClick = { navController.navigate(FirstNameScreen) }) {
            Text("Don't have an account? Sign Up", color = Color.LightGray)
        }
    }
}

private fun checkUserInFirestore(navController: NavController, username: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    db.collection("Users").document(uid).get()
        .addOnSuccessListener { doc ->
            if (doc != null && doc.exists() &&
                doc.getString("username") == username) {
                navController.navigate(HomeScreen) {
                    popUpTo(LoginSignupScreen) { inclusive = true }
                }
            } else {
                FirebaseAuth.getInstance().signOut()
                Log.e("FIREBASE", "Invalid user or username mismatch")
            }
        }
        .addOnFailureListener { e ->
            FirebaseAuth.getInstance().signOut()
            Log.e("FIREBASE", "Failed to retrieve user", e)
        }
}


@Composable
fun FirstNameScreenFunction(firstName: MutableState<String>, navController: NavController){
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
            textStyle = TextStyle(color = Color.White)
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

@Composable
fun BirthdayScreenFunction(birthday: MutableState<String>, navController: NavController){
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
            textStyle = TextStyle(color = Color.White)
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

@Composable
fun PhoneNumberScreenFunction(phoneNumber: MutableState<String>, navController: NavController) {
    val verificationId = remember { mutableStateOf<String?>(null) }
    val smsCode = remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (verificationId.value == null) {
            Text("Enter Phone Number", color = Color.White)
            OutlinedTextField(
                value = phoneNumber.value,
                onValueChange = { phoneNumber.value = it },
                label = { Text("Phone Number") },
                textStyle = TextStyle(color = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber(phoneNumber.value)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(context as Activity)
                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                FirebaseAuth.getInstance().signInWithCredential(credential)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            navController.navigate(UsernameScreen)
                                        }
                                    }
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                Log.e("PhoneAuth", "Verification failed", e)
                            }

                            override fun onCodeSent(verificationIdString: String, token: PhoneAuthProvider.ForceResendingToken) {
                                verificationId.value = verificationIdString
                            }
                        })
                        .build()

                    PhoneAuthProvider.verifyPhoneNumber(options)
                },
                enabled = phoneNumber.value.isNotBlank()
            ) {
                Text("Send Code")
            }
        } else {
            Text("Enter Verification Code", color = Color.White)
            OutlinedTextField(
                value = smsCode.value,
                onValueChange = { smsCode.value = it },
                label = { Text("Verification Code") },
                textStyle = TextStyle(color = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val credential = PhoneAuthProvider.getCredential(verificationId.value!!, smsCode.value)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                navController.navigate(UsernameScreen)
                            } else {
                                Log.e("PhoneAuth", "Sign in failed", it.exception)
                            }
                        }
                },
                enabled = smsCode.value.length == 6
            ) {
                Text("Verify & Continue")
            }
        }
    }
}


@Composable
fun UsernameScreenFunction(
    firstName: MutableState<String>,
    birthday: MutableState<String>,
    phoneNumber: MutableState<String>,
    username: MutableState<String>,
    navController: NavController
)
 {
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
            textStyle = TextStyle(color = Color.White)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val db = Firebase.firestore
                val auth = FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    val userData = hashMapOf(
                        "firstName" to firstName.value,
                        "birthday" to birthday.value,
                        "phoneNumber" to phoneNumber.value,
                        "username" to username.value
                    )

                    db.collection("Users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            navController.navigate(HomeScreen) {
                                popUpTo(FirstNameScreen) { inclusive = true }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FIRESTORE", "Failed to save user data", e)
                        }
                } else {
                    Log.e("AUTH", "User not logged in")
                }
            }

            ,
            enabled = username.value.isNotBlank()
        ) {
            Text("Finish")
        }
    }
}

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
    //NowPlay Logo
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "NowPlay.",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
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
            text = "Friend Screen",
            color = Color.White
        )
    }

    var searchText by remember { mutableStateOf("") }
    //NowPlay Logo
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "NowPlay.",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(14.dp))
        //Search Friends button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
        }


        //Invite friends link box
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(Color.DarkGray),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
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
                        text = "Invite friends on BeReal",
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
    val context = LocalContext.current
    val navController = rememberNavController()


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

            Text(
                username,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
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
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        },
                        content = { Text("Logout", color = Color.White, fontSize = 20.sp) },
                        colors = ButtonDefaults.buttonColors(Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
