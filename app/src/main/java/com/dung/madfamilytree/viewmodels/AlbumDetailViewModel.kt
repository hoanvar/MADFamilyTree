package com.dung.madfamilytree.viewmodels

import SupabaseClientProvider
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dung.madfamilytree.dtos.AlbumDTO
import com.dung.madfamilytree.dtos.ImageDTO
import com.dung.madfamilytree.models.Image
import com.dung.madfamilytree.utility.Utility
import com.dung.madfamilytree.views.activities.AlbumDetailActivity.Companion.ALBUM_ID
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumDetailViewModel : ViewModel() {

    var recycleMode = MutableLiveData<SelectMode>(SelectMode.ONE)
    var editable = false
    var albumId = ""
        set(value) {
            field = value
            viewModelScope.launch(Dispatchers.IO) {
                isEditable()
                getAlbumInfo()
                getAlbumImage()
            }

        }
    val album = MutableLiveData<AlbumDTO>(null)
    private val _imageList = MutableLiveData<List<ImageDTO>>()
    val imageList: LiveData<List<ImageDTO>>
        get() = _imageList

    fun setImageList(images: List<ImageDTO>) {
        _imageList.value = images
    }

    fun addImages(context: Context, images: List<Image>) {
        val tempImageList = _imageList.value?.toMutableList()
        for (i in 0 until images.size) {
            tempImageList?.add(ImageDTO(url = "holder"))
        }
        _imageList.postValue(tempImageList?.toList())
        viewModelScope.launch(Dispatchers.IO) {
            for (image in images) {
                val url = SupabaseClientProvider.uploadImageFromUri(context, image.ImageURI!!)
                url?.let {
                    Utility.db?.collection("Image")
                        ?.add(
                            hashMapOf(
                                "url" to it,
                                "album" to Utility.db?.collection("Album")?.document(albumId),
                                "uploadTime" to FieldValue.serverTimestamp()
                            )
                        )
                }
            }
            getAlbumImage()
        }
    }

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
    }


    fun getAlbumImage() {

        Utility.db?.collection("Image")
            ?.whereEqualTo("album", Utility.db?.collection("Album")?.document(albumId))
            ?.orderBy("uploadTime", Query.Direction.ASCENDING)
            ?.get()
            ?.addOnSuccessListener { result ->
                val tempImageList = mutableListOf<ImageDTO>()
                if (editable) {
                    tempImageList.add(ImageDTO())
                }
//                    mutableListOf(ImageDTO())
//                    else mutableListOf()
                for (imageSnapshot in result) {
                    val image = imageSnapshot.toObject(ImageDTO::class.java)
                    tempImageList.add(image)
                }
                _imageList.value = tempImageList
            }
            ?.addOnFailureListener {
                val tempImageList = mutableListOf<ImageDTO>()
                if (editable) {
                    tempImageList.add(ImageDTO())
                }
                _imageList.value = tempImageList
            }

    }

    suspend fun isEditable() {
        editable = Utility.isEditable(Utility.accountId, albumId)
    }
}

enum class SelectMode(val type: String) {
    MULTI("MUTI"), ONE("ONE")
}