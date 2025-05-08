package com.dung.madfamilytree.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dung.madfamilytree.dtos.AlbumDTO
import com.dung.madfamilytree.dtos.InvokingDTO
import com.dung.madfamilytree.models.Album
import com.dung.madfamilytree.utility.Utility
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlbumViewModel(private val db: FirebaseFirestore) : ViewModel() {
    private val _albumList = MutableLiveData<MutableList<AlbumDTO>>(mutableListOf(AlbumDTO(name = "Thêm mới Album")))
    val albumList: LiveData<out List<AlbumDTO>>
        get() = _albumList

    fun getAlbum(accountId: String = "qeCGzYEwV5w7VYpWXtdn") {
        viewModelScope.launch(Dispatchers.IO) {
            _albumList.postValue(Utility.getAlbum().toMutableList())

        //            db.collection("Invoking")
//                .whereEqualTo("account", db.collection("Account").document(accountId))
//                .get()
//                .addOnSuccessListener { result ->
//                    val tempAlbumList = mutableListOf(AlbumDTO())
//                    for (doc in result) {
//                        val invoking = doc.toObject(InvokingDTO::class.java)
//                        invoking.album?.get()
//                            ?.addOnSuccessListener { albumSnapshot ->
//                                Utility.db?.collection("Image")
//                                    ?.whereEqualTo("album", invoking.album)
//                                    ?.get()
//                                    ?.addOnSuccessListener { result ->
//                                        val album = albumSnapshot.toObject(AlbumDTO::class.java)
//                                        album?.let {
//                                            it.id = albumSnapshot.id
//                                            if (!result.isEmpty) {
//                                                it.thumbnail_img = result.iterator().next().reference
//                                            }
//                                            tempAlbumList.add(it)
//                                            _albumList.value = tempAlbumList
//                                        }
//                                    }
//                            }
//                    }
//                }
        }

    }
}