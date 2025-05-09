package com.dung.madfamilytree.views.activities
import AddressManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityCreateNewTreeBinding
import com.dung.madfamilytree.databinding.ActivityHomeNotInTreeBinding
import com.dung.madfamilytree.dtos.TreeDTO
//import com.dung.madfamilytree.utils.AddressManager
import com.dung.madfamilytree.utility.Utility
import kotlinx.coroutines.launch

class CreateNewTreeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCreateNewTreeBinding
    private val addressManager = AddressManager()
    private var selectedProvinceCode: Int? = null
    private var selectedDistrictCode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewTreeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAddressSpinners()
        setupSaveButton()
        //        setUpBottonNavBar()
        setUpEvent()

    }
    fun setUpEvent() {
        binding.createNewTree.btnSave.setOnClickListener {
            createNewTree()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

        private fun setupAddressSpinners() {
        lifecycleScope.launch {
            try {
                // Load provinces
                val provinces = addressManager.loadProvinces()
                val provinceAdapter = ArrayAdapter(
                    this@CreateNewTreeActivity,
                    R.layout.spinner_item,
                    provinces.map { it.name }
                )
                binding.createNewTree.spinnerProvince.setAdapter(provinceAdapter)

                // Setup province spinner listener
                binding.createNewTree.spinnerProvince.setOnItemClickListener { _, _, position, _ ->
                    selectedProvinceCode = provinces[position].code
                    updateDistrictSpinner()
                }

                // Setup district spinner listener
                binding.createNewTree.spinnerDistrict.setOnItemClickListener { _, _, position, _ ->
                    selectedDistrictCode = addressManager.getDistricts(selectedProvinceCode!!)[position].code
                    updateWardSpinner()
                }

            } catch (e: Exception) {
                Toast.makeText(this@CreateNewTreeActivity, "Lỗi khi tải dữ liệu địa chỉ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDistrictSpinner() {
        selectedProvinceCode?.let { provinceCode ->
            val districts = addressManager.getDistricts(provinceCode)
            val districtAdapter = ArrayAdapter(
                this,
                R.layout.spinner_item,
                districts.map { it.name }
            )
            binding.createNewTree.spinnerDistrict.setAdapter(districtAdapter)
        }
    }

    private fun updateWardSpinner() {
        if (selectedProvinceCode != null && selectedDistrictCode != null) {
            val wards = addressManager.getWards(selectedProvinceCode!!, selectedDistrictCode!!)
            val wardAdapter = ArrayAdapter(
                this,
                R.layout.spinner_item,
                wards.map { it.name }
            )
            binding.createNewTree.spinnerWard.setAdapter(wardAdapter)
        }
    }

    private fun setupSaveButton() {
        binding.createNewTree.btnSave.setOnClickListener {
            createNewTree()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun createNewTree() {
        val introduce = binding.createNewTree.edtDescription.text.toString()
        val province = binding.createNewTree.spinnerProvince.text.toString()
        val district = binding.createNewTree.spinnerDistrict.text.toString()
        val commune = binding.createNewTree.spinnerWard.text.toString()
        val tree_name = binding.createNewTree.edtFamilyName.text.toString()
        val exact_address = binding.createNewTree.edtAddressDetail.text.toString()
        
        val treeDTO = TreeDTO(
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
}