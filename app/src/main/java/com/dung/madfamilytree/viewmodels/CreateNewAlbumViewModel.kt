package com.dung.madfamilytree.viewmodels

import SupabaseClientProvider
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dung.madfamilytree.models.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateNewAlbumViewModel : ViewModel() {
    private val _imageList = MutableLiveData<MutableList<Image>>()
    val imageUrlList = mutableListOf<String>()
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
    suspend fun upLoadImages(context: Context){
            imageList.value?.let{
                for(img in it){
                    img.ImageURI?.let {
                        val imageUrl = SupabaseClientProvider.uploadImageFromUri(context,it)
                        imageUrl?.let{
                            imageUrlList.add(it)
                        }
                    }
                }
            }
    }
}