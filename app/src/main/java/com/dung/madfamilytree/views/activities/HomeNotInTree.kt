package com.dung.madfamilytree.views.activities

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityHomeBinding
import com.dung.madfamilytree.databinding.ActivityHomeNotInTreeBinding

class HomeNotInTree : BaseActivity() {
    private lateinit var binding : ActivityHomeNotInTreeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeNotInTreeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpBottonNavBar()
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
    fun setUpBottonNavBar(){
        binding.bottomNavBarCustom.bottomNavView.setupWithNavController((supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment) as NavHostFragment).navController)
    }
}