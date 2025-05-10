package com.example.nowplay

import android.util.Base64
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Response
import org.json.JSONException
import org.json.JSONObject
import android.content.Context
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.net.Uri

// used to convert the selected image to base64 as an encoded string
fun encodeImageToBase64(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val imageBytes = inputStream?.readBytes()
    inputStream?.close()
    return Base64.encodeToString(imageBytes, Base64.DEFAULT)
}

// now we need to upload the image to imgur since we don't have firebase storage
fun uploadImageToImgur(base64Image: String, context: Context, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    val url = "https://api.imgur.com/3/image"
    val requestQueue = Volley.newRequestQueue(context)

    val stringRequest = object : StringRequest(Method.POST, url,
        Response.Listener { response ->
            try {
                val jsonResponse = JSONObject(response)
                val data = jsonResponse.getJSONObject("data")
                val imageUrl = data.getString("link")
                onSuccess(imageUrl)
            } catch (e: JSONException) {
                onError("Error parsing response: ${e.message}")
            }
        },
        Response.ErrorListener { error ->
            onError("Error uploading image: ${error.message}")
        }
    ) {
        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] = "Client-ID adca9cd92fdb6e9"
            return headers
        }

        override fun getParams(): Map<String, String> {
            val params = HashMap<String, String>()
            params["image"] = base64Image
            return params
        }
    }

    requestQueue.add(stringRequest)
}


// now that we have the imgur link for the pfp, we can set it as the image url in the firestore
// so we can use pfps
fun updateUserProfileImage(userId: String, imageUrl: String) {
    val userRef = Firebase.firestore.collection("Users").document(userId)
    userRef.update("profileImageUrl", imageUrl)
        .addOnSuccessListener {
            Log.d("FIRESTORE", "Image URL updated successfully")
        }
        .addOnFailureListener { e ->
            Log.w("FIRESTORE", "Error updating image URL", e)
        }
}