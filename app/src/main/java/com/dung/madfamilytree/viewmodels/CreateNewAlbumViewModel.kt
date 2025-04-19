package com.dung.madfamilytree.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dung.madfamilytree.models.Image

class CreateNewAlbumViewModel : ViewModel() {
    private val _imageList = MutableLiveData<MutableList<Image>>()
    val imageList: LiveData<out List<Image>>
        get() = _imageList
    fun setImageList(images: List<Image>){
        _imageList.value = images.toMutableList()
    }
    fun addImageList(images: List<Image>){
        _imageList.value = _imageList.value?.plus(images)?.toMutableList()
    }
    fun deleteImage(image: Image){
        _imageList.value?.remove(image)
        _imageList.value = _imageList.value?.toMutableList()
    }
}