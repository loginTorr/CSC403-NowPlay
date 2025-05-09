package com.example.nowplay

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

@Serializable
object LoginSignupScreen
@Serializable
object LoginScreen
@Serializable
object FirstNameScreen
@Serializable
object BirthdayScreen
@Serializable
object PhoneNumberScreen
@Serializable
object UsernameScreen
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
@Serializable
object ViewPostScreen
@Serializable
object SettingsScreen
@Serializable
object EditProfileScreen

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
