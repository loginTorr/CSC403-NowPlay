package com.example.nowplay

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date


@IgnoreExtraProperties
data class Post(
    var userId:      String = "",
    var songName:    String = "",
    var artistName:  String = "",
    var albumName:   String = "",
    var songPicture: String = "",
    var timeStamp:   Date   = Date()
)
