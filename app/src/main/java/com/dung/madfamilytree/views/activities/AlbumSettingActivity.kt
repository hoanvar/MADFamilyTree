package com.dung.madfamilytree.views.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityAlbumSettingBinding
import com.dung.madfamilytree.databinding.DeleteImagePopupBinding
import com.dung.madfamilytree.dtos.ImageDTO
import com.dung.madfamilytree.utility.Utility
import com.dung.madfamilytree.viewmodels.AlbumSettingViewModel
import com.dung.madfamilytree.views.activities.ImageDetailActivity.Companion.imageUrl
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlbumSettingActivity : BaseActivity() {
    companion object{
        const val ALBUM_ID = "ALBUM_ID"
        const val EDITABLE = "EDITABLE"
        const val DELETED = "DELETED"
        const val OWNER = "ONWER"
    }

    val updateAlbumRegistorForActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if(result.resultCode == RESULT_OK)
        {
            val data = result.data
            data?.let{
                if(it.getBooleanExtra(UpdateAlbumActivity.UPDATED,false)){
                    Snackbar.make(binding.root, "Update Successful", Snackbar.LENGTH_SHORT).show()
                }
                else {
                    Snackbar.make(binding.root, "Update Fail", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
    private lateinit var viewModel: AlbumSettingViewModel
    private lateinit var binding: ActivityAlbumSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AlbumSettingViewModel::class.java)
        viewModel.albumId = intent.getStringExtra(ALBUM_ID)!!
        viewModel.editable = intent.getBooleanExtra(EDITABLE,false)
        viewModel.owner = intent.getBooleanExtra(OWNER,false)

        binding = ActivityAlbumSettingBinding.inflate(layoutInflater)

        binding.viewModel = viewModel
        setSupportActionBar(binding.albumSettingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Cài đặt Album"
        setContentView(binding.root)

        viewModel.finishDeleted.observe(this, Observer {
            val resultIntent = Intent()
            resultIntent.putExtra(DELETED,it)
            setResult(RESULT_OK,resultIntent)
            finish()
        })
        binding.updateAlbumBtn.setOnClickListener {
            val intent = Intent(this,UpdateAlbumActivity::class.java)
            intent.putExtra(UpdateAlbumActivity.ALBUM_ID,viewModel.albumId)
            updateAlbumRegistorForActivityResult.launch(intent)
        }

        binding.share.setOnClickListener {

        }

        binding.deleteAlbumBtn.setOnClickListener {
            val localBinding = DeleteImagePopupBinding.inflate(layoutInflater)
            val popupWindow = PopupWindow(
                localBinding.root,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            localBinding.textView9.text = "Xóa Album"

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
                viewModel.deleteAlbum()
                popupWindow.dismiss()


            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}