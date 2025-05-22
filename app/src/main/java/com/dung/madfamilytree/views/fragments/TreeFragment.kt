package com.dung.madfamilytree.views.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var currentDialogView: View

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedAvatarUri = it
            val avatarImageView = currentDialogView.findViewById<ImageView>(R.id.profile_avatar)
            Glide.with(requireContext())
                .load(uri)
                .placeholder(R.drawable.profile_icon)
                .into(avatarImageView)
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
                profileAvatarUrl = node.profile?.avatar_url ?: ""
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

    private fun showAddPartnerDialog(node: TreeNode) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_partner, null)
        val dialog = AlertDialog.Builder(requireContext())
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
                educational_level = dialogView.findViewById<EditText>(R.id.et_educational_level).text.toString(),
                job = dialogView.findViewById<EditText>(R.id.et_job).text.toString(),
                province1 = dialogView.findViewById<EditText>(R.id.et_province1).text.toString(),
                district1 = dialogView.findViewById<EditText>(R.id.et_district1).text.toString(),
                commune1 = dialogView.findViewById<EditText>(R.id.et_commune1).text.toString(),
                province2 = dialogView.findViewById<EditText>(R.id.et_province2).text.toString(),
                district2 = dialogView.findViewById<EditText>(R.id.et_district2).text.toString(),
                commune2 = dialogView.findViewById<EditText>(R.id.et_commune2).text.toString(),
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
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding partner", e)
                    Toast.makeText(context, "Lỗi khi thêm vợ/chồng", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddChildDialog(node: TreeNode) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_partner, null)
        val dialog = AlertDialog.Builder(requireContext())
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

        // Setup avatar selection
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
            val gender = genderSpinner.selectedItem?.toString() ?: "Nam"
            val phoneNumber = dialogView.findViewById<EditText>(R.id.et_phone_number)?.text?.toString() ?: ""
            val maritalStatus = maritalStatusSpinner.selectedItem?.toString() ?: "Độc thân"
            val educationalLevel = dialogView.findViewById<EditText>(R.id.et_educational_level)?.text?.toString() ?: ""
            val job = dialogView.findViewById<EditText>(R.id.et_job)?.text?.toString() ?: ""
            val province1 = dialogView.findViewById<EditText>(R.id.et_province1)?.text?.toString() ?: ""
            val district1 = dialogView.findViewById<EditText>(R.id.et_district1)?.text?.toString() ?: ""
            val commune1 = dialogView.findViewById<EditText>(R.id.et_commune1)?.text?.toString() ?: ""
            val province2 = dialogView.findViewById<EditText>(R.id.et_province2)?.text?.toString() ?: ""
            val district2 = dialogView.findViewById<EditText>(R.id.et_district2)?.text?.toString() ?: ""
            val commune2 = dialogView.findViewById<EditText>(R.id.et_commune2)?.text?.toString() ?: ""
            var avatarUrl: String? = null

            val profile = ProfileDTO(
                id = profileId,
                name = name,
                another_name = anotherName,
                gender = gender,
                date_of_birth = Timestamp(selectedDate!!.time),
                phone_number = phoneNumber,
                marital_status = maritalStatus,
                educational_level = educationalLevel,
                job = job,
                province1 = province1,
                district1 = district1,
                commune1 = commune1,
                province2 = province2,
                district2 = district2,
                commune2 = commune2,
                died = 0,
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
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding child", e)
                    Log.e(TAG, "Error details: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(context, "Lỗi khi thêm con: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }
} 