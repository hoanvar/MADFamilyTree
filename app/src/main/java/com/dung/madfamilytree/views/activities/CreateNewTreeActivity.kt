package com.dung.madfamilytree.views.activities
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityCreateNewTreeBinding
import com.dung.madfamilytree.databinding.ActivityHomeNotInTreeBinding
import com.dung.madfamilytree.dtos.TreeDTO
import com.dung.madfamilytree.utility.Utility
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CreateNewTreeActivity : BaseActivity() {
    private lateinit var binding : ActivityCreateNewTreeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewTreeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpBottonNavBar()
        setUpEvent()
    }
    fun setUpEvent(){
       binding.createNewTree.btnSave.setOnClickListener {
           createNewTree()
           val intent = Intent(this, HomeActivity::class.java)
           intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
           startActivity(intent)
           finish()
       }
    }

    fun createNewTree(){
        val introduce = binding.createNewTree.edtDescription.text.toString()
        val province = binding.createNewTree.etThanhpho.text.toString()
        val district = binding.createNewTree.etHuyen.text.toString()
        val commune = binding.createNewTree.etXa.text.toString()
        val tree_name = binding.createNewTree.edtFamilyName.text.toString()
        val exact_address = binding.createNewTree.edtAddressDetail.text.toString()
        val treeDTO = TreeDTO (
            introduce = introduce,
            tree_name = tree_name,
            province = province,
            district = district,
            commune = commune,
            exact_address = exact_address,
            id_root = null
        )

        Utility.db?.collection("Tree")
            ?.add(treeDTO)
            ?.addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Tree added with ID: ${documentReference.id}")
                
                // Update account's tree_id
                Utility.db?.collection("Account")
                    ?.document(Utility.accountId)
                    ?.update("tree_id", documentReference.id)
                    ?.addOnSuccessListener {
                        Log.d("Firestore", "Account tree_id updated successfully")
                    }
                    ?.addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating account tree_id", e)
                    }
            }
            ?.addOnFailureListener { e ->
                Log.w("Firestore", "Error adding new tree", e)
            }
    }
    fun setUpBottonNavBar(){
        binding.bottomNavBarCustom.bottomNavView.setupWithNavController((supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment) as NavHostFragment).navController)
    }

}