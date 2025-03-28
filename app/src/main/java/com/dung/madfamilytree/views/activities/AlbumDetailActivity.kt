package com.dung.madfamilytree.views.activities

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dung.madfamilytree.R
import com.dung.madfamilytree.adapters.AlbumImageItemAdapter
import com.dung.madfamilytree.databinding.ActivityAlbumDetailBinding
import com.dung.madfamilytree.models.Image

class AlbumDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityAlbumDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ContextCompat.getString(this,R.string.album_item_text)

        val adapter = AlbumImageItemAdapter()
        adapter.data = listOf(Image("hello"),Image("Hello"))
        binding.albumImageRecycleView.adapter = adapter
    }
}