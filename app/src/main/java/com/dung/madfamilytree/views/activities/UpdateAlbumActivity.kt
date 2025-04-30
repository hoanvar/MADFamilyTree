package com.dung.madfamilytree.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityUpdateAlbumBinding
import com.dung.madfamilytree.viewmodels.UpdateAlbumViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay

class UpdateAlbumActivity : BaseActivity() {
    companion object{
        const val ALBUM_ID = "ALBUM_ID"
        const val UPDATED = "UPDATED"
    }
    private lateinit var binding: ActivityUpdateAlbumBinding
    private lateinit var viewModel: UpdateAlbumViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Chỉnh sửa album"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(UpdateAlbumViewModel::class.java)

        viewModel.albumId = intent.getStringExtra(ALBUM_ID)!!
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.updated.observe(this, Observer {
            it?.let{
                val resultIntent = Intent()
                resultIntent.putExtra(UPDATED,it)
                setResult(RESULT_OK,resultIntent)
                finish()
                viewModel.updated.value = null
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}