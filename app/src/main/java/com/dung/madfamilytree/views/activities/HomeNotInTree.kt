package com.dung.madfamilytree.views.activities

import android.content.Intent
import android.os.Bundle
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityHomeNotInTreeBinding

class HomeNotInTree : BaseActivity() {
    private lateinit var binding : ActivityHomeNotInTreeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeNotInTreeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpEvent()
    }

    fun setUpEvent(){
        binding.fab.setOnClickListener{
            val intent = Intent(this, CreateNewTreeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}