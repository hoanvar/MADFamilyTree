package com.dung.madfamilytree.views.activities

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dung.madfamilytree.R
import com.dung.madfamilytree.adapters.AccountItemAdapter
import com.dung.madfamilytree.databinding.ActivityAlbumGrandPermissionBinding
import com.dung.madfamilytree.databinding.GrandPermissionPopupBinding
import com.dung.madfamilytree.dtos.InvokingDTO
import com.dung.madfamilytree.utility.Utility
import com.dung.madfamilytree.viewmodels.AlbumGrandPermissionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlbumGrandPermissionActivity : BaseActivity() {
    companion object{
        const val ALBUM_ID = "ALBUM_ID"
    }
    private lateinit var binding: ActivityAlbumGrandPermissionBinding
    private lateinit var viewModel: AlbumGrandPermissionViewModel
    private lateinit var adapter: AccountItemAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumGrandPermissionBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(AlbumGrandPermissionViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
        setUpComponent()
        adapter = AccountItemAdapter(){accountId,username ->
            Utility.db?.let{
                it.collection("Invoking")
                    .whereEqualTo("album",it.collection("Album").document(intent.getStringExtra(
                        ALBUM_ID)!!))
                    .whereEqualTo("account",it.collection("Account").document(accountId))
                    .get()
                    .addOnSuccessListener { result->
                        if(result.isEmpty){
                            showPopup(accountId,username,false,true)
                        }
                        else {
                            val invokingSnapshot = result.iterator().next()
                            val invokingDTO = invokingSnapshot.toObject(InvokingDTO::class.java)
                            showPopup(accountId,username,invokingDTO.editable,false, invokingSnapshot.id)
                        }
                    }
            }

        }
        binding.accountRecycleView.adapter = adapter
        viewModel.accounts.observe(this, Observer {
            adapter.data = it
        })
    }

    fun showPopup(accountId: String,userName: String, editable: Boolean,createNew: Boolean,invokingId: String = ""){
        val localBinding = GrandPermissionPopupBinding.inflate(layoutInflater)
        val popupWindow = PopupWindow(
            localBinding.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        localBinding.grandFor.append(" $userName")
        localBinding.checkBox.isChecked = editable

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        val container = popupWindow.contentView.parent as View

        popupWindow.contentView.setOnApplyWindowInsetsListener { v, insets ->
            v.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
            insets
        }


        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = container.layoutParams as WindowManager.LayoutParams
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.5f // Set dim level (0.0 = no dim, 1.0 = full dim)
        windowManager.updateViewLayout(container, layoutParams)
        localBinding.deleteBtn.setOnClickListener {
            popupWindow.dismiss()
        }
        localBinding.confirmBtn.setOnClickListener {
            if(createNew){
                Utility.db?.let {
                    it.collection("Invoking").add(
                        hashMapOf(
                            "album" to it.collection("Album").document(intent.getStringExtra(
                                ALBUM_ID)!!),
                            "account" to it.collection("Account").document(accountId),
                            "editable" to localBinding.checkBox.isChecked
                        )
                    )
                }
            }
            else{
                Utility.db?.let {
                    it.collection("Invoking")
                        .document(invokingId)
                        .update(
                            mapOf(
                                "editable" to localBinding.checkBox.isChecked
                            )
                        )
                }
            }

            popupWindow.dismiss()


        }
    }

    fun setUpComponent(){
        setSupportActionBar(binding.toobar)
        supportActionBar?.title = "Cấp quyền Album"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.accountSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.startTime = System.currentTimeMillis()
                viewModel.searching = false
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        lifecycleScope.launch(Dispatchers.Default) {
            while (true){
                delay(100)
                if(!viewModel.searching && System.currentTimeMillis() - viewModel.startTime > 1000){
                    viewModel.searching = true
                    viewModel.searchAccount()
                }
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}