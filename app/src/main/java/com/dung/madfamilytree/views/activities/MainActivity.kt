package com.dung.madfamilytree.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.dung.madfamilytree.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.startBtn.setOnClickListener {
            Log.d("MainActivity","button clicked")
            val intent = Intent(this@MainActivity,LoginActivity::class.java)
            startActivity(intent)
        }
    }
    fun setUpEvent(){
        binding.startBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity,LoginActivity::class.java))
        }

    }
}

