package com.dung.madfamilytree.views.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityHomeBinding
import com.dung.madfamilytree.databinding.TreeSummerizeBinding
import com.dung.madfamilytree.utility.Utility
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
//    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup bottom navigation
        binding.bottomNavBarCustom.bottomNavView.setupWithNavController((supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController)
        
        // Launch coroutine to call suspend function
        coroutineScope.launch {
            setUp()
        }
    }
    
    private suspend fun setUp(){
        val tree_id = Utility.getTreeId()
        Log.d("HomeActivity", "Tree ID from account: $tree_id")
        
        if (tree_id == "false" || tree_id.isEmpty()) {
            Log.e("HomeActivity", "Invalid tree ID")
            binding.treeSummary.tvFamilyName.text = ""
            binding.treeSummary.tvAddress.text = ""
            binding.treeSummary.tvGenerations.text = "0 đời"
            binding.treeSummary.tvMembers.text = "0 thành viên"
            return
        }

        try {
            val treeDocument = Utility.db?.collection("Tree")
                ?.document(tree_id)
                ?.get()
                ?.await()

            if (treeDocument != null) {
                if (treeDocument.exists()) {
                    val treeData = treeDocument.data
                    Log.d("HomeActivity", "Tree data: $treeData")

                    // Update UI with tree data
                    treeData?.let { data ->
                        binding.treeSummary.tvFamilyName.text = data["tree_name"] as? String ?: ""
                        binding.treeSummary.tvAddress.text = "Địa chỉ: Xã " + data["commune"] + " Huyện " + data["commune"] + " Tỉnh " + data["province"]
                        binding.treeSummary.tvGenerations.text = "${data["generations"] as? Long ?: 0} đời"
                        binding.treeSummary.tvMembers.text = "${data["members"] as? Long ?: 0} thành viên"
                    }
                } else {
                    Log.e("HomeActivity", "Tree document does not exist")
                    // Document doesn't exist, set default values
                    binding.treeSummary.tvFamilyName.text = ""
                    binding.treeSummary.tvAddress.text = ""
                    binding.treeSummary.tvGenerations.text = "0 đời"
                    binding.treeSummary.tvMembers.text = "0 thành viên"
                }
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error getting tree data", e)
            // Handle any errors that occur during the query
            e.printStackTrace()
            // Set default values in case of error
            binding.treeSummary.tvFamilyName.text = ""
            binding.treeSummary.tvAddress.text = ""
            binding.treeSummary.tvGenerations.text = "0 đời"
            binding.treeSummary.tvMembers.text = "0 thành viên"
        }
    }
}