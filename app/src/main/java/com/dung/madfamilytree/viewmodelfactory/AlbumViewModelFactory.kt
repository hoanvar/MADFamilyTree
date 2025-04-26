package com.dung.madfamilytree.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dung.madfamilytree.viewmodels.AlbumViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AlbumViewModelFactory(private  val db: FirebaseFirestore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AlbumViewModel::class.java)){
            return AlbumViewModel(db) as T
        }
        else throw IllegalArgumentException("Unkonw ViewModel!")
    }
}