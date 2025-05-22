package com.dung.madfamilytree.views.fragments

import AddressManager
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.FragmentTreeBinding
import com.dung.madfamilytree.dtos.NodeDTO
import com.dung.madfamilytree.dtos.ProfileDTO
import com.dung.madfamilytree.dtos.TreeNode
import com.dung.madfamilytree.utility.TreeUtility
import com.dung.madfamilytree.utility.Utility
import com.dung.madfamilytree.views.custom.FamilyTreeView
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class TreeFragment : Fragment() {
    private val TAG = "TreeFragment"
    private lateinit var familyTreeView: FamilyTreeView
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var _binding: FragmentTreeBinding? = null
    private val binding get() = _binding!!
    private var selectedAvatarUri: Uri? = null
    private lateinit var currentDialog: AlertDialog
//    private lateinit var currentDialogView: View
    private var currentDialogView: View? = null

//    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        uri?.let {
//            selectedAvatarUri = it
//            var avatarImageView = currentDialogView.findViewById<ImageView>(R.id.profile_avatar)
//            Glide.with(requireContext())
//                .load(uri)
//                .placeholder(R.drawable.profile_icon)
//                .into(avatarImageView)
//        }
//    }
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedAvatarUri = it
            currentDialogView?.let { dialogView ->  // Kiểm tra null safety
                val avatarImageView = dialogView.findViewById<ImageView>(R.id.profile_avatar)
                Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.profile_icon)
                    .into(avatarImageView)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentTreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        
        // Set up back button click listener
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Set up family tree button click listener
        binding.btnFamilyTree.setOnClickListener {
            findNavController().navigate(R.id.action_tree_to_familyTreeFragment)
        }

        familyTreeView = view.findViewById(R.id.familyTreeView)
        val treeId = Utility.treeId
        Log.d(TAG, "TreeId: $treeId")

        // Set up node click listener
        familyTreeView.setOnNodeClickListener { node ->
            // Navigate to profile card with the selected profile
            val action = TreeFragmentDirections.actionTreeFragmentToProfileCardFragment(
                profileId = node.profileId,
                profileName = node.profile?.name ?: "",
                profileAnotherName = node.profile?.another_name ?: "",
                profileGender = node.profile?.gender ?: "",
                profileDateOfBirth = node.profile?.date_of_birth?.toDate()?.let { dateFormat.format(it) } ?: "",
                profilePhoneNumber = node.profile?.phone_number ?: "",
                profileMaritalStatus = node.profile?.marital_status ?: "",
                profileEducationalLevel = node.profile?.educational_level ?: "",
                profileJob = node.profile?.job ?: "",
                profileProvince1 = node.profile?.province1 ?: "",
                profileDistrict1 = node.profile?.district1 ?: "",
                profileCommune1 = node.profile?.commune1 ?: "",
                profileProvince2 = node.profile?.province2 ?: "",
                profileDistrict2 = node.profile?.district2 ?: "",
                profileCommune2 = node.profile?.commune2 ?: "",
                profileDied = node.profile?.died ?: 0,
                profileBiography = node.profile?.biography ?: "",
                profileAvatarUrl = node.profile?.avatar_url ?: "",
                profileTimeDied = node.profile?.death_anniversary?.toDate()?.let { dateFormat.format(it) } ?: "",
                profileTimeDiedWas = node.profile?.date_of_death?.toDate()?.let { dateFormat.format(it) } ?: "",
                profileAgeAtDied = node.profile?.age_at_death ?: 0,
                profileBurialInfo = node.profile?.burial_info ?: ""
            )
            findNavController().navigate(action)
        }

        // Set up add partner button click listener
        familyTreeView.setOnAddPartnerClickListener { node ->
            showAddPartnerDialog(node)
        }

        // Set up add child button click listener
        familyTreeView.setOnAddChildClickListener { node ->
            showAddChildDialog(node)
        }

        // Set up tree name
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val treeName = Utility.getTreeName()
            } catch (e: Exception) {
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(TAG, "Starting to fetch family tree data")
            val (nodes, profiles) = TreeUtility.fetchFamilyTree(treeId)
            Log.d(TAG, "Fetched nodes count: ${nodes?.size}")
            Log.d(TAG, "Fetched profiles count: ${profiles.size}")
            
            nodes?.let {
                val rootId = Utility.rootId
                Log.d(TAG, "RootId: $rootId")
                val treeRoot = TreeUtility.buildTree(rootId, it, profiles)
                Log.d(TAG, "Tree built successfully: ${treeRoot != null}")
                TreeUtility.printTreeToLog(treeRoot)
                
                // Set the tree in our custom view
                treeRoot?.let { root ->
                    familyTreeView.setTree(root)
                }
            } ?: run {
                Log.e(TAG, "No nodes found for treeId: $treeId")
                Toast.makeText(context, "No family tree data found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddPartnerDialog(node: TreeNode) {
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_partner, null)
//        val dialog = AlertDialog.Builder(requireContext())
//            .setView(dialogView)
//            .setCancelable(true)
//            .create()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_partner, null)
        currentDialogView = dialogView  // Gán giá trị cho currentDialogView
        currentDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set up gender spinner
        val genderSpinner = dialogView.findViewById<Spinner>(R.id.spinner_gender)
        val genderAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        genderSpinner.adapter = genderAdapter
        // Setup education level spinner
        val educationSpinner = dialogView.findViewById<Spinner>(R.id.spinner_education)
        val educationAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.education_option,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        educationSpinner.adapter = educationAdapter
        // Set up marital status spinner
        val maritalStatusSpinner = dialogView.findViewById<Spinner>(R.id.spinner_marital_status)
        val maritalStatusAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.marital_status_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        maritalStatusSpinner.adapter = maritalStatusAdapter

        // Set up date picker for date of birth
        val dateOfBirthEdit = dialogView.findViewById<EditText>(R.id.et_date_of_birth)
        var selectedDate: Calendar? = null
        dateOfBirthEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar
                    dateOfBirthEdit.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // Setup địa chỉ nguyên quán (Address 1)
        val spinnerProvince1 = dialogView.findViewById<Spinner>(R.id.spinner_province1)
        val spinnerDistrict1 = dialogView.findViewById<Spinner>(R.id.spinner_district1)
        val spinnerCommune1 = dialogView.findViewById<Spinner>(R.id.spinner_commune1)

        // Setup địa chỉ hiện tại (Address 2)
        val spinnerProvince2 = dialogView.findViewById<Spinner>(R.id.spinner_province2)
        val spinnerDistrict2 = dialogView.findViewById<Spinner>(R.id.spinner_district2)
        val spinnerCommune2 = dialogView.findViewById<Spinner>(R.id.spinner_commune2)

        // Load provinces và setup spinners
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val addressManager = AddressManager()
                val provinces = addressManager.loadProvinces()
                val provinceNames = provinces.map { it.name }

                // Setup Province 1 (Nguyên quán)
                val provinceAdapter1 = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    provinceNames
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerProvince1.adapter = provinceAdapter1

                // Setup Province 2 (Địa chỉ hiện tại)
                val provinceAdapter2 = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    provinceNames
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerProvince2.adapter = provinceAdapter2

                // Listener cho Province 1
                spinnerProvince1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedProvince = provinces[position]
                        val districts = addressManager.getDistricts(selectedProvince.code)
                        val districtNames = districts.map { it.name }

                        val districtAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            districtNames
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        spinnerDistrict1.adapter = districtAdapter

                        // Listener cho District 1
                        spinnerDistrict1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                                val selectedDistrict = districts[pos]
                                val wards = addressManager.getWards(selectedProvince.code, selectedDistrict.code)
                                val wardNames = wards.map { it.name }

                                val wardAdapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_spinner_item,
                                    wardNames
                                ).apply {
                                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                }
                                spinnerCommune1.adapter = wardAdapter
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                // Listener cho Province 2
                spinnerProvince2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedProvince = provinces[position]
                        val districts = addressManager.getDistricts(selectedProvince.code)
                        val districtNames = districts.map { it.name }

                        val districtAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            districtNames
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        spinnerDistrict2.adapter = districtAdapter

                        // Listener cho District 2
                        spinnerDistrict2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                                val selectedDistrict = districts[pos]
                                val wards = addressManager.getWards(selectedProvince.code, selectedDistrict.code)
                                val wardNames = wards.map { it.name }

                                val wardAdapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_spinner_item,
                                    wardNames
                                ).apply {
                                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                }
                                spinnerCommune2.adapter = wardAdapter
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading address data", e)
                Toast.makeText(context, "Lỗi khi tải dữ liệu địa chỉ", Toast.LENGTH_SHORT).show()
            }
        }
        //         Setup avatar selection
        dialogView.findViewById<Button>(R.id.btn_add_avatar).setOnClickListener {
            pickImage.launch("image/*")
        }

        // Set up save button
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val name = dialogView.findViewById<EditText>(R.id.et_name).text.toString()
            if (name.isBlank()) {
                Toast.makeText(context, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate == null) {
                Toast.makeText(context, "Vui lòng chọn ngày sinh", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create new profile
            val profileId = UUID.randomUUID().toString()
            var avatarUrl: String? = null

            val profile = ProfileDTO(
                id = profileId,
                name = name,
                another_name = dialogView.findViewById<EditText>(R.id.et_another_name).text.toString(),
                gender = genderSpinner.selectedItem.toString(),
                date_of_birth = Timestamp(selectedDate!!.time),
                phone_number = dialogView.findViewById<EditText>(R.id.et_phone_number).text.toString(),
                marital_status = maritalStatusSpinner.selectedItem.toString(),
                educational_level = educationSpinner.selectedItem?.toString(),
                job = dialogView.findViewById<EditText>(R.id.et_job).text.toString(),
                province1 = spinnerProvince1.selectedItem?.toString() ?: "",
                district1 = spinnerDistrict1.selectedItem?.toString() ?: "",
                commune1 = spinnerCommune1.selectedItem?.toString() ?: "",
                province2 = spinnerProvince2.selectedItem?.toString() ?: "",
                district2 = spinnerDistrict2.selectedItem?.toString() ?: "",
                commune2 = spinnerCommune2.selectedItem?.toString() ?: "",
                biography = dialogView.findViewById<EditText>(R.id.et_biography).text.toString(),
                died = 0,
                avatar_url = avatarUrl
            )

            // Save profile and update node
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Save profile to Firestore
                    Utility.db?.collection("Profile")
                        ?.document(profileId)
                        ?.set(profile)
                        ?.await()
                    var avatarUrl: String? = null
                    selectedAvatarUri?.let { uri ->
                        avatarUrl = SupabaseClientProvider.uploadImageFromUri(requireContext(), uri)
                        profile.avatar_url = avatarUrl
                    }

                    // Update node with partner
                    val nodeRef = Utility.db?.collection("Node")
                        ?.whereEqualTo("id_profile", node.profileId)
                        ?.whereEqualTo("id_tree", Utility.treeId)
                        ?.get()
                        ?.await()
                        ?.documents
                        ?.firstOrNull()

                    nodeRef?.reference?.update("id_partner", profileId)?.await()

                    // Refresh tree view
                    val (nodes, profiles) = TreeUtility.fetchFamilyTree(Utility.treeId)
                    nodes?.let {
                        val rootId = Utility.rootId
                        val treeRoot = TreeUtility.buildTree(rootId, it, profiles)
                        treeRoot?.let { root ->
                            familyTreeView.setTree(root)
                        }
                    }

                    Toast.makeText(context, "Thêm vợ/chồng thành công", Toast.LENGTH_SHORT).show()
                    currentDialog.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding partner", e)
                    Toast.makeText(context, "Lỗi khi thêm vợ/chồng", Toast.LENGTH_SHORT).show()
                }
            }
        }

        currentDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddChildDialog(node: TreeNode) {
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_partner, null)
//        val dialog = AlertDialog.Builder(requireContext())
//            .setView(dialogView)
//            .setCancelable(true)
//            .create()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_partner, null)
        currentDialogView = dialogView  // Gán giá trị cho currentDialogView
        currentDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set up gender spinner
        val genderSpinner = dialogView.findViewById<Spinner>(R.id.spinner_gender)
        val genderAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        genderSpinner.adapter = genderAdapter

        // Setup education level spinner
        val educationSpinner = dialogView.findViewById<Spinner>(R.id.spinner_education)
        val educationAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.education_option,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        educationSpinner.adapter = educationAdapter


        // Set up marital status spinner
        val maritalStatusSpinner = dialogView.findViewById<Spinner>(R.id.spinner_marital_status)
        val maritalStatusAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.marital_status_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        maritalStatusSpinner.adapter = maritalStatusAdapter

        // Set up date picker for date of birth
        val dateOfBirthEdit = dialogView.findViewById<EditText>(R.id.et_date_of_birth)
        var selectedDate: Calendar? = null
        dateOfBirthEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar
                    dateOfBirthEdit.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // Setup địa chỉ nguyên quán (Address 1)
        val spinnerProvince1 = dialogView.findViewById<Spinner>(R.id.spinner_province1)
        val spinnerDistrict1 = dialogView.findViewById<Spinner>(R.id.spinner_district1)
        val spinnerCommune1 = dialogView.findViewById<Spinner>(R.id.spinner_commune1)

        // Setup địa chỉ hiện tại (Address 2)
        val spinnerProvince2 = dialogView.findViewById<Spinner>(R.id.spinner_province2)
        val spinnerDistrict2 = dialogView.findViewById<Spinner>(R.id.spinner_district2)
        val spinnerCommune2 = dialogView.findViewById<Spinner>(R.id.spinner_commune2)

        // Load provinces và setup spinners
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val addressManager = AddressManager()
                val provinces = addressManager.loadProvinces()
                val provinceNames = provinces.map { it.name }

                // Setup Province 1 (Nguyên quán)
                val provinceAdapter1 = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    provinceNames
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerProvince1.adapter = provinceAdapter1

                // Setup Province 2 (Địa chỉ hiện tại)
                val provinceAdapter2 = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    provinceNames
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerProvince2.adapter = provinceAdapter2

                // Listener cho Province 1
                spinnerProvince1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedProvince = provinces[position]
                        val districts = addressManager.getDistricts(selectedProvince.code)
                        val districtNames = districts.map { it.name }

                        val districtAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            districtNames
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        spinnerDistrict1.adapter = districtAdapter

                        // Listener cho District 1
                        spinnerDistrict1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                                val selectedDistrict = districts[pos]
                                val wards = addressManager.getWards(selectedProvince.code, selectedDistrict.code)
                                val wardNames = wards.map { it.name }

                                val wardAdapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_spinner_item,
                                    wardNames
                                ).apply {
                                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                }
                                spinnerCommune1.adapter = wardAdapter
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                // Listener cho Province 2
                spinnerProvince2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedProvince = provinces[position]
                        val districts = addressManager.getDistricts(selectedProvince.code)
                        val districtNames = districts.map { it.name }

                        val districtAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            districtNames
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        spinnerDistrict2.adapter = districtAdapter

                        // Listener cho District 2
                        spinnerDistrict2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                                val selectedDistrict = districts[pos]
                                val wards = addressManager.getWards(selectedProvince.code, selectedDistrict.code)
                                val wardNames = wards.map { it.name }

                                val wardAdapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_spinner_item,
                                    wardNames
                                ).apply {
                                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                }
                                spinnerCommune2.adapter = wardAdapter
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading address data", e)
                Toast.makeText(context, "Lỗi khi tải dữ liệu địa chỉ", Toast.LENGTH_SHORT).show()
            }
        }

//         Setup avatar selection
        dialogView.findViewById<Button>(R.id.btn_add_avatar).setOnClickListener {
            pickImage.launch("image/*")
        }

        // Set up save button
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val name = dialogView.findViewById<EditText>(R.id.et_name)?.text?.toString() ?: ""
            if (name.isBlank()) {
                Toast.makeText(context, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate == null) {
                Toast.makeText(context, "Vui lòng chọn ngày sinh", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create new profile
            val profileId = UUID.randomUUID().toString()
            Log.d(TAG, "Creating new profile with ID: $profileId")
            
            // Get all fields with null safety
            val anotherName = dialogView.findViewById<EditText>(R.id.et_another_name)?.text?.toString() ?: ""
            val phoneNumber = dialogView.findViewById<EditText>(R.id.et_phone_number)?.text?.toString() ?: ""
            val job = dialogView.findViewById<EditText>(R.id.et_job)?.text?.toString() ?: ""
            var avatarUrl: String? = null

            val profile = ProfileDTO(
                id = profileId,
                name = name,
                another_name = anotherName,
                gender = genderSpinner.selectedItem?.toString() ?: "",
                date_of_birth = Timestamp(selectedDate!!.time),
                phone_number = phoneNumber,
                marital_status = maritalStatusSpinner.selectedItem?.toString() ?: "",
                educational_level = educationSpinner.selectedItem?.toString() ?: "",
                job = job,
                province1 = spinnerProvince1.selectedItem?.toString() ?: "",
                district1 = spinnerDistrict1.selectedItem?.toString() ?: "",
                commune1 = spinnerCommune1.selectedItem?.toString() ?: "",
                province2 = spinnerProvince2.selectedItem?.toString() ?: "",
                district2 = spinnerDistrict2.selectedItem?.toString() ?: "",
                commune2 = spinnerCommune2.selectedItem?.toString() ?: "",
                died = 0,
                biography = dialogView.findViewById<EditText>(R.id.et_biography).text.toString(),
                avatar_url = avatarUrl
            )
            Log.d(TAG, "Profile object created: $profile")

            // Create new node
            val nodeId = UUID.randomUUID().toString()
            Log.d(TAG, "Creating new node with ID: $nodeId")
            val newNode = NodeDTO(
                id = nodeId,
                id_profile = profileId,
                id_tree = Utility.treeId,
                id_partner = null,
                id_children = null
            )
            Log.d(TAG, "Node object created: $newNode")

            // Save profile and node
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    Log.d(TAG, "Starting to save profile to Firestore...")
                    // Save profile to Firestore
                    Utility.db?.collection("Profile")
                        ?.document(profileId)
                        ?.set(profile)
                        ?.await()
                    var avatarUrl: String? = null
                    selectedAvatarUri?.let { uri ->
                        avatarUrl = SupabaseClientProvider.uploadImageFromUri(requireContext(), uri)
                        profile.avatar_url = avatarUrl
                    }
                    Log.d(TAG, "Profile saved successfully to Firestore")


                    Log.d(TAG, "Starting to save node to Firestore...")
                    // Save node to Firestore
                    Utility.db?.collection("Node")
                        ?.document(nodeId)
                        ?.set(newNode)
                        ?.await()
                    Log.d(TAG, "Node saved successfully to Firestore")

                    Log.d(TAG, "Finding parent node with profileId: ${node.profileId} and treeId: ${Utility.treeId}")
                    // Update parent node's children list
                    val parentNodeRef = Utility.db?.collection("Node")
                        ?.whereEqualTo("id_profile", node.profileId)
                        ?.whereEqualTo("id_tree", Utility.treeId)
                        ?.get()
                        ?.await()
                        ?.documents
                        ?.firstOrNull()

                    if (parentNodeRef == null) {
                        Log.e(TAG, "Parent node not found!")
                        throw Exception("Parent node not found")
                    }

                    Log.d(TAG, "Parent node found: ${parentNodeRef.data}")
                    val currentChildren = parentNodeRef.get("id_children") as? List<String> ?: emptyList()
                    Log.d(TAG, "Current children list: $currentChildren")
                    val updatedChildren = currentChildren + profileId
                    Log.d(TAG, "Updated children list: $updatedChildren")

                    Log.d(TAG, "Updating parent node's children list...")
                    parentNodeRef.reference?.update("id_children", updatedChildren)?.await()
                    Log.d(TAG, "Parent node updated successfully")

                    Log.d(TAG, "Refreshing tree view...")
                    // Refresh tree view
                    val (nodes, profiles) = TreeUtility.fetchFamilyTree(Utility.treeId)
                    nodes?.let {
                        val rootId = Utility.rootId
                        val treeRoot = TreeUtility.buildTree(rootId, it, profiles)
                        treeRoot?.let { root ->
                            familyTreeView.setTree(root)
                        }
                    }
                    Log.d(TAG, "Tree view refreshed successfully")

                    Toast.makeText(context, "Thêm con thành công", Toast.LENGTH_SHORT).show()
                    currentDialog.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding child", e)
                    Log.e(TAG, "Error details: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(context, "Lỗi khi thêm con: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        currentDialog.show()
    }
} 