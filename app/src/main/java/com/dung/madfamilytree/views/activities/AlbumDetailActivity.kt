package com.dung.madfamilytree.views.activities

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dung.madfamilytree.R
import com.dung.madfamilytree.adapters.AlbumImageItemAdapter
import com.dung.madfamilytree.databinding.ActivityAlbumDetailBinding
import com.dung.madfamilytree.dtos.AlbumDTO
import com.dung.madfamilytree.models.Image
import com.dung.madfamilytree.utility.Utility
import com.dung.madfamilytree.viewmodels.AlbumDetailViewModel
import com.dung.madfamilytree.viewmodels.SelectMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumDetailActivity : BaseActivity() {
    companion object {
        const val ALBUM_ID = "ALBUM_ID"
    }

    private lateinit var binding: ActivityAlbumDetailBinding
    private lateinit var viewModel: AlbumDetailViewModel
    val pickMutipleImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            handleSelectedImages(uris)
        }
    private lateinit var adapter: AlbumImageItemAdapter

    fun handleSelectedImages(URIs: List<Uri>) {
        val imageList = mutableListOf<Image>()
        for (uri in URIs) {
            imageList.add(Image("", uri))
        }
        viewModel.addImages(this, imageList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        Utility.db?.collection("Album")
//            ?.document(intent.getStringExtra(ALBUM_ID)!!)
//            ?.get()
//            ?.addOnSuccessListener { albumSnapshot ->
//                val albumDto = albumSnapshot.toObject(AlbumDTO::class.java)
//                albumDto?.let{
//                    binding.placeTv.text = albumDto.place
//                    binding.editTextTextMultiLine.text.append(albumDto.story)
//                }
//            }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ContextCompat.getString(this, R.string.album_item_text)
//        binding.buttonBox.visibility = View.INVISIBLE

        binding.cancleBtn.setOnClickListener {
            viewModel.recycleMode.value = SelectMode.ONE
            AlbumImageItemAdapter.AlbumImageItemViewHolder.clearSelectedImage()
        }
        binding.deleteImgBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    viewModel.recycleMode.postValue(SelectMode.ONE)
                }

                Utility.deleteImageList(AlbumImageItemAdapter.AlbumImageItemViewHolder.getSelectedImageList(),{})
                viewModel.getAlbumImage()
            }
        }
        viewModel = ViewModelProvider(this).get(AlbumDetailViewModel::class.java)
        adapter = AlbumImageItemAdapter({
            if (it.url == "") {
                pickMutipleImages.launch("image/*")
            } else {
                val intent = Intent(this, ImageDetailActivity::class.java)

                intent.putExtra(ImageDetailActivity.IMAGE_URI, it.url.toString())
                startActivity(intent)
            }
        }, viewModel)
        viewModel.albumId = intent.getStringExtra(ALBUM_ID)!!

        viewModel.album.observe(this, Observer {
            if (it != null) {
                binding.placeTv.text = it.place
                binding.editTextTextMultiLine.text.clear()
                binding.editTextTextMultiLine.text.append(it.story)
                supportActionBar?.title = it.name
            }
        })
        viewModel.imageList.observe(this, Observer {
            it?.let {
                Log.d("AlbumDetailActivity", "Get images")

                adapter.submitList(it)
            }
        })

        viewModel.recycleMode.observe(this, Observer {
            adapter.notifyDataSetChanged()
            binding.buttonBox.visibility = if (it == SelectMode.ONE) View.GONE else View.VISIBLE
        })
        binding.albumImageRecycleView.adapter = adapter
        binding.albumSettingBtn.setOnClickListener {
            val intent = Intent(this, AlbumSettingActivity::class.java)
            intent.putExtra(AlbumSettingActivity.ALBUM_ID, viewModel.albumId)
            intent.putExtra(AlbumSettingActivity.EDITABLE, viewModel.editable)
            intent.putExtra(AlbumSettingActivity.OWNER,viewModel.album.value?.owner?.equals(Utility.db?.collection("Account")?.document(Utility.accountId)))
            albumSettingRegistor.launch(intent)
        }
    }

    val albumSettingRegistor =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                Log.d("AlbumDetailActivity", "result")
                if (data?.getBooleanExtra(AlbumSettingActivity.DELETED, false)!!) {
                    finish()
                }
            } else {
                Log.d("AlbumDetailActivity", "Load data")
                viewModel.getAlbumInfo()
                viewModel.getAlbumImage()
            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }


    override fun onRestart() {
        super.onRestart()
        viewModel.getAlbumInfo()
        viewModel.getAlbumImage()

    }
}