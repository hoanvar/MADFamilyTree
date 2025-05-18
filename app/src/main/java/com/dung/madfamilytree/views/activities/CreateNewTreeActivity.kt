package com.dung.madfamilytree.views.activities
import AddressManager
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityCreateNewTreeBinding
import com.dung.madfamilytree.databinding.ActivityHomeNotInTreeBinding
import com.dung.madfamilytree.dtos.NodeDTO
import com.dung.madfamilytree.dtos.ProfileDTO
import com.dung.madfamilytree.dtos.TreeDTO
//import com.dung.madfamilytree.utils.AddressManager
import com.dung.madfamilytree.utility.Utility
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreateNewTreeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCreateNewTreeBinding
    private val addressManager = AddressManager()
    private var selectedProvinceCode: Int? = null
    private var selectedDistrictCode: Int? = null
    private var selectedDate: Date? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
                        Utility.treeId = documentReference.id
                        showCreateRootProfileDialog()
                    }
                    ?.addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating account tree_id", e)
                        Toast.makeText(this, "Lỗi khi cập nhật thông tin tài khoản", Toast.LENGTH_SHORT).show()
                    }
            }
            ?.addOnFailureListener { e ->
                Log.w("Firestore", "Error adding new tree", e)
                Toast.makeText(this, "Lỗi khi tạo gia phả mới", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCreateRootProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_root_profile, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Setup gender spinner
        val genderSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinner_gender)
        val genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        genderSpinner.adapter = genderAdapter

        // Setup marital status spinner
        val maritalStatusSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinner_marital_status)
        val maritalStatusAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.marital_status_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        maritalStatusSpinner.adapter = maritalStatusAdapter

        // Setup date picker
        val dateOfBirthEditText = dialogView.findViewById<android.widget.EditText>(R.id.et_date_of_birth)
        dateOfBirthEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    dateOfBirthEditText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Setup save button
        dialogView.findViewById<android.widget.Button>(R.id.btn_save).setOnClickListener {
            val name = dialogView.findViewById<android.widget.EditText>(R.id.et_name)?.text?.toString() ?: ""
            if (name.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate == null) {
                Toast.makeText(this, "Vui lòng chọn ngày sinh", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create new profile
            val profileId = UUID.randomUUID().toString()
            val profile = ProfileDTO(
                id = profileId,
                name = name,
                gender = genderSpinner.selectedItem?.toString() ?: "Nam",
                date_of_birth = Timestamp(selectedDate!!.time / 1000, ((selectedDate!!.time % 1000) * 1000000).toInt()),
                phone_number = dialogView.findViewById<android.widget.EditText>(R.id.et_phone_number)?.text?.toString() ?: "",
                marital_status = maritalStatusSpinner.selectedItem?.toString() ?: "Độc thân",
                educational_level = dialogView.findViewById<android.widget.EditText>(R.id.et_educational_level)?.text?.toString() ?: "",
                job = dialogView.findViewById<android.widget.EditText>(R.id.et_job)?.text?.toString() ?: "",
                province1 = binding.createNewTree.spinnerProvince.text.toString(),
                district1 = binding.createNewTree.spinnerDistrict.text.toString(),
                commune1 = binding.createNewTree.spinnerWard.text.toString(),
                province2 = binding.createNewTree.spinnerProvince.text.toString(),
                district2 = binding.createNewTree.spinnerDistrict.text.toString(),
                commune2 = binding.createNewTree.spinnerWard.text.toString(),
                died = 0
            )

            // Create new node
            val nodeId = UUID.randomUUID().toString()
            val newNode = NodeDTO(
                id = nodeId,
                id_profile = profileId,
                id_tree = Utility.treeId,
                id_partner = null,
                id_children = null
            )

            // Save profile and node
            lifecycleScope.launch {
                try {
                    // Save profile to Firestore
                    Utility.db?.collection("Profile")
                        ?.document(profileId)
                        ?.set(profile)
                        ?.await()

                    // Save node to Firestore
                    Utility.db?.collection("Node")
                        ?.document(nodeId)
                        ?.set(newNode)
                        ?.await()

                    // Update tree's root_id
                    Utility.db?.collection("Tree")
                        ?.document(Utility.treeId)
                        ?.update("id_root", profileId)
                        ?.await()

                    Utility.rootId = profileId
                    dialog.dismiss()
                    
                    // Navigate to home activity
                    val intent = Intent(this@CreateNewTreeActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e("CreateNewTreeActivity", "Error saving root profile", e)
                    Toast.makeText(this@CreateNewTreeActivity, "Lỗi khi lưu thông tin người khởi đầu", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }
}