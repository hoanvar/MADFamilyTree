package com.dung.madfamilytree.views.activities

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityImageDetailBinding
import com.dung.madfamilytree.databinding.DeleteImagePopupBinding
import com.dung.madfamilytree.dtos.ImageDTO
import com.dung.madfamilytree.utility.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.HandlerDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageDetailActivity : BaseActivity() {
    companion object {
        const val IMAGE_URI = "image_uri"
        var imageUrl = ""
    }

    private lateinit var binding: ActivityImageDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setUpImage()
        setUpToolbar()
        setUpEvent()
    }

    fun setUpImage() {
        imageUrl = intent.getStringExtra(IMAGE_URI)!!
        Glide.with(binding.imageView.context)
            .load(imageUrl)
            .placeholder(R.drawable.default_icon_img)
            .fitCenter()
            .into(binding.imageView)
    }

    fun setUpEvent() {
        binding.deleteBtn.setOnClickListener {
            val localBinding = DeleteImagePopupBinding.inflate(layoutInflater)
            val popupWindow = PopupWindow(
                localBinding.root,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

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
                popupWindow.dismiss()
                lifecycleScope.launch(Dispatchers.IO)
                {
                    Utility.deleteImageList(listOf(ImageDTO(url = imageUrl)))
                    finish()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    override fun onStart() {
        super.onStart()
        binding.imageContainer.post {
            setUpComponent()
        }
    }

    fun setUpComponent() {
        setUpImage()
        val layoutParams = binding.imageView.layoutParams
        layoutParams.width = binding.imageContainer.width
        layoutParams.height = binding.imageContainer.height
        binding.imageView.layoutParams = layoutParams
    }

    fun setUpToolbar() {
        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title =
            ContextCompat.getString(this, R.string.image_detail_activity_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}