package com.dung.madfamilytree.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dung.madfamilytree.dtos.AccountDTO
import com.dung.madfamilytree.utility.Utility

class AlbumGrandPermissionViewModel : ViewModel() {
    var startTime:Long = 0
    var searching = false
    val userName = MutableLiveData<String>("")
    private val _account = MutableLiveData<List<AccountDTO>>()
    val accounts: LiveData<List<AccountDTO>>
        get() = _account

    fun searchAccount(){
        val searchText = userName.value
        Utility.db?.let {
            it.collection("Account")
                .whereEqualTo("username", userName.value)
                .get()
                .addOnSuccessListener { result ->
                    val accountsList = mutableListOf<AccountDTO>()
                    for(accountSnapshot in result){
                        val accountDTO = accountSnapshot.toObject(AccountDTO::class.java)
                        accountDTO.id = accountSnapshot.id
                        accountsList.add(accountDTO)
                    }
                    if (accountsList.isNotEmpty()) {
                        _account.value = accountsList
                    }
                }
        }
    }
}