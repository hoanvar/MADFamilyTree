package com.dung.madfamilytree.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dung.madfamilytree.models.Image

class AlbumDetailViewModel : ViewModel() {
    private val _imageList  = MutableLiveData<List<Image>>()
    val imageList: LiveData<List<Image>>
        get() = _imageList
    fun setImageList(images:List<Image>){
        _imageList.value = images
    }
    fun addImages(images: List<Image>){
        val list = _imageList.value
        _imageList.value = list?.plus(images)
    }
}