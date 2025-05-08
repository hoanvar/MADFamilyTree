package com.dung.madfamilytree.dtos

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class ImageDTO(
    val album: DocumentReference? = null,
    val url: String = "",
//    val uploadTime: Timestamp? = null
)
