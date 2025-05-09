package com.dung.madfamilytree.views.activities

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityHomeBinding
import com.dung.madfamilytree.utility.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup bottom navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavBarCustom.bottomNavView.setupWithNavController(navController)

        // Prevent recreation of fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment) {
                // If we're navigating to HomeFragment, check if user has a tree
                CoroutineScope(Dispatchers.Main).launch {
                    val treeId = Utility.getTreeId()
                    if (treeId == "false" || treeId.isEmpty()) {
                        navToHomeNotInTree()
                    }
                }
            }
        }
    }

    fun navToHomeNotInTree() {
        val intent = Intent(this, HomeNotInTree::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}