package com.dung.madfamilytree.views.activities

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dung.madfamilytree.R
import com.dung.madfamilytree.adapters.AlbumImageItemAdapter
import com.dung.madfamilytree.databinding.ActivityAlbumDetailBinding
import com.dung.madfamilytree.models.Image
import com.dung.madfamilytree.viewmodels.AlbumDetailViewModel

class AlbumDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityAlbumDetailBinding
    private lateinit var viewModel: AlbumDetailViewModel
    val pickMutipleImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()){ uris ->
        handleSelectedImages(uris)
    }
    val adapter = AlbumImageItemAdapter {
        if(it.ImageURI == null){
            pickMutipleImages.launch("image/*")
        }
        else {
            val intent = Intent(this,ImageDetailActivity::class.java)
            
            intent.putExtra(ImageDetailActivity.IMAGE_URI,it.ImageURI.toString())
            startActivity(intent)
        }
    }
    fun handleSelectedImages(URIs: List<Uri>){
        val imageList = mutableListOf<Image>()
        for(uri in URIs){
            imageList.add(Image("",uri))
        }
        viewModel.addImages(imageList)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ContextCompat.getString(this,R.string.album_item_text)
        viewModel = ViewModelProvider(this).get(AlbumDetailViewModel::class.java)
        viewModel.setImageList(listOf(Image("hello", null)))
        viewModel.imageList.observe(this, Observer {
            it?.let{
                Log.d("AlbumDetailActivity","Get images")

                adapter.submitList(it)
            }
        })
        binding.albumImageRecycleView.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}