package com.dung.madfamilytree.dtos

import com.google.firebase.firestore.DocumentReference

data class AlbumDTO(var id: String = "",val name:String="", val place: String = "", val story: String = "", var thumbnail_img: DocumentReference? = null )
