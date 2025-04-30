package com.dung.madfamilytree.dtos

import com.google.firebase.firestore.DocumentReference

data class InvokingDTO(
    val album: DocumentReference? = null,
    val account: DocumentReference? = null,
    val editable: Boolean = true
)
