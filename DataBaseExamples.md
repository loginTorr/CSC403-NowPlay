
//the imports I know are needed, there might be more
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue


//create User class with attributes as it's parameters
@IgnoreExtraProperties
data class User(val username: String? = null, val phoneNumber: Long? = 0) {

}

@IgnoreExtraProperties
data class UserData(val username: String? = null, val phoneNumber: Long? = 0) {

}

//declare above MainActivity
private lateinit var database: DatabaseReference
private lateinit var UserReference: DatabaseReference


class MainActivity : ComponentActivity() {
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //initialize database reference under setContent 
            database = Firebase.database.reference

            //calls func to create new database object
            writeNewUser("Log", "Logan Torr", 2259360141)

            //get database data
            UserReference.addValueEventListener(UserListener)


        }
}

//write function called outside main class
fun writeNewUser(userId: String, name: String, phoneNumber: Long) {
    val user = User(name, phoneNumber)

    //sets new User as a child of the Users table
    database.child("Users").child(userId).setValue(user)
}


//read data from database
val UserListener = object : ValueEventListener {
    override fun onDataChange(dataSnapshot: DataSnapshot) {
    // Get Post object and use the values to update the UI
    val userData = dataSnapshot.getValue<UserData>()
    // ...
    }
    
    //if no data found
    override fun onCancelled(databaseError: DatabaseError) {
        // Getting Post failed, log a message
        Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
    }
}