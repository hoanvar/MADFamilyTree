package com.dung.madfamilytree.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dung.madfamilytree.utility.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlbumSettingViewModel : ViewModel() {
    var albumId = ""
    var editable = false
    var owner = false
    val finishDeleted = MutableLiveData<Boolean>()
    fun deleteAlbum(){
        viewModelScope.launch(Dispatchers.IO) {
            Utility.db?.let {
                it.collection("Image").
                        whereEqualTo("album",it.collection("Album").document(albumId))
                    .get()
                    .addOnSuccessListener { resultSnapShot ->
                        for(imageSnapShot in resultSnapShot){
                            imageSnapShot.reference.delete()
                        }
                        it.collection("Invoking")
                            .whereEqualTo("album",it.collection("Album").document(albumId))
                            .get()
                            .addOnSuccessListener { resultSnapShot ->
                                for(invokingSnapShot in resultSnapShot)
                                {
                                    invokingSnapShot.reference.delete()
                                }
                                it.collection("Album").document(albumId).delete()
                                finishDeleted.value = true
                            }
                    }
            }
        }
    }
}