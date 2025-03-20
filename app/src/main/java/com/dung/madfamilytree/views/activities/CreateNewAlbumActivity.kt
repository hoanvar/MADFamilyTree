package com.dung.madfamilytree.views.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityCreateNewAlbumBinding

class CreateNewAlbumActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateNewAlbumBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpToolBar()


    }
    fun setUpToolBar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tạo mới album"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}