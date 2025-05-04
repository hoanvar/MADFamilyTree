package com.dung.madfamilytree.views.activities

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup bottom navigation
        binding.bottomNavBarCustom.bottomNavView.setupWithNavController((supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController)
    }
}