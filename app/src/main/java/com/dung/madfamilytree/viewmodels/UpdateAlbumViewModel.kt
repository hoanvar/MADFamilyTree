package com.dung.madfamilytree.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.dung.madfamilytree.dtos.AlbumDTO
import com.dung.madfamilytree.utility.Utility
import com.google.android.material.snackbar.Snackbar

class UpdateAlbumViewModel : ViewModel() {
    var albumId = ""
        set(value) {
            field = value
            getAlbumInfo()
        }
    val updated = MutableLiveData<Boolean?>(null)
    val album = MutableLiveData<AlbumDTO?>()
    val albumName = album.map { it?.name ?: "" } as MutableLiveData
    val albumPlace = album.map { it?.place ?: "" } as MutableLiveData
    val albumStory = album.map { it?.story ?: "" } as MutableLiveData
    fun getAlbumInfo() {
        Utility.db?.collection("Album")
            ?.document(albumId)
            ?.get()
            ?.addOnSuccessListener { albumSnapshot ->
                val albumDto = albumSnapshot.toObject(AlbumDTO::class.java)
                albumDto?.let {
                    album.value = it
                }
            }
            ?.addOnFailureListener {
                album.value = null
            }
    }
    fun updateAlbum(){
        Utility.db?.let {
            it.collection("Album")
                .document(albumId)
                .update(mapOf(
                    "name" to albumName.value,
                    "place" to albumPlace.value,
                    "story" to albumStory.value
                ))
                .addOnSuccessListener {
                    updated.value = true
                }
                .addOnFailureListener {
                    updated.value = false
                }
        }
    }
}