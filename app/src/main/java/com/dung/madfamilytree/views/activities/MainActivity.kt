package com.dung.madfamilytree.views.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.dung.madfamilytree.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    fun setUpEvent(){
        binding.startBtn.setOnClickListener {

        }
    }
}

