package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dung.madfamilytree.databinding.FragmentFamilyTreeBinding
import com.dung.madfamilytree.databinding.ProfileCardBinding
import com.dung.madfamilytree.dtos.NodeDTO
import com.dung.madfamilytree.dtos.ProfileDTO
import com.dung.madfamilytree.dtos.TreeNode
import com.dung.madfamilytree.utility.TreeUtility
import com.dung.madfamilytree.utility.Utility
import com.dung.madfamilytree.views.custom.FamilyTreeView
import com.dung.madfamilytree.adapters.DepthAdapter
import com.dung.madfamilytree.adapters.ProfileAdapter
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.dung.madfamilytree.R

class FamilyTreeFragment : Fragment() {
    private val TAG = "FamilyTreeFragment"
    private var _binding: FragmentFamilyTreeBinding? = null
    private val binding get() = _binding!!
    private lateinit var depthAdapter: DepthAdapter
    private lateinit var profileAdapter: ProfileAdapter
    private var selectedDepth = 0
    private var depthList: List<String> = listOf()
    private var profilesByDepth: Map<Int, List<TreeNode>> = mapOf()
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyTreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycleViewDoi.layoutManager = LinearLayoutManager(requireContext())
        binding.recycleViewProfiles.layoutManager = LinearLayoutManager(requireContext())
        
        // Set up back button click listener
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Set up tree button click listener
        binding.btnTree.setOnClickListener {
            findNavController().navigate(R.id.action_familyTreeFragment_to_tree)
        }

        loadFamilyTree()
    }

    private fun loadFamilyTree() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val treeId = Utility.getTreeId()
                val (nodes, profiles) = TreeUtility.fetchFamilyTree(treeId)
                val rootId = Utility.rootId
                val treeRoot = nodes?.let { TreeUtility.buildTree(rootId, it, profiles) }

                profilesByDepth = TreeUtility.groupProfilesByDepth(treeRoot)
                depthList = profilesByDepth.keys.sorted().map { "Đời thứ ${it + 1}" }
                selectedDepth = 0

                depthAdapter = DepthAdapter(depthList, selectedDepth) { index ->
                    selectedDepth = index
                    depthAdapter.notifyDataSetChanged()
                    profileAdapter = ProfileAdapter(profilesByDepth[index] ?: emptyList()) { node ->
                        showAddRelationshipDialog(node.profileId, node.profile?.name ?: "")
                    }
                    binding.recycleViewProfiles.adapter = profileAdapter
                }
                binding.recycleViewDoi.adapter = depthAdapter

                profileAdapter = ProfileAdapter(profilesByDepth[selectedDepth] ?: emptyList()) { node ->
                    showAddRelationshipDialog(node.profileId, node.profile?.name ?: "")
                }
                binding.recycleViewProfiles.adapter = profileAdapter

            } catch (e: Exception) {
                Log.e(TAG, "Error loading family tree", e)
            }
        }
    }

    private fun showAddRelationshipDialog(profileId: String, profileName: String) {
        val options = arrayOf("Thêm con", "Thêm vợ/chồng")
        AlertDialog.Builder(requireContext())
            .setTitle("Thêm quan hệ cho $profileName")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddProfileDialog(profileId, "child")
                    1 -> checkAndShowAddPartnerDialog(profileId, profileName)
                }
            }
            .show()
    }

    private fun checkAndShowAddPartnerDialog(profileId: String, profileName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val node = db.collection("Node").document(profileId).get().await()
                val nodeDTO = node.toObject(NodeDTO::class.java)
                
                if (nodeDTO?.id_partner == null) {
                    showAddProfileDialog(profileId, "partner")
                } else {
                    Toast.makeText(requireContext(), "Người này đã có vợ/chồng", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking partner status", e)
                Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddProfileDialog(parentId: String, relationshipType: String) {
        val dialogBinding = ProfileCardBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setNegativeButton("Hủy", null)
            .create()

        // Setup marital status spinner
        val maritalStatuses = arrayOf("Độc thân", "Đã kết hôn", "Ly hôn", "Góa")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, maritalStatuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerMaritalStatus.adapter = adapter

        // Set up save button click listener
        dialogBinding.btnSave.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Create new profile
                    val newProfileId = UUID.randomUUID().toString()
                    val dateOfBirth = try {
                        val date = dateFormat.parse(dialogBinding.etDateOfBirth.text.toString())
                        Timestamp(date ?: Date())
                    } catch (e: Exception) {
                        null
                    }
                    
                    val profile = ProfileDTO(
                        id = newProfileId,
                        name = dialogBinding.etName.text.toString(),
                        another_name = dialogBinding.etAnotherName.text.toString(),
                        gender = dialogBinding.etGender.text.toString(),
                        date_of_birth = dateOfBirth,
                        phone_number = dialogBinding.etPhoneNumber.text.toString(),
                        marital_status = dialogBinding.spinnerMaritalStatus.selectedItem.toString(),
                        educational_level = dialogBinding.etEducationalLevel.text.toString(),
                        job = dialogBinding.etJob.text.toString(),
                        province1 = dialogBinding.etProvince1.text.toString(),
                        district1 = dialogBinding.etDistrict1.text.toString(),
                        commune1 = dialogBinding.etCommune1.text.toString(),
                        province2 = dialogBinding.etProvince2.text.toString(),
                        district2 = dialogBinding.etDistrict2.text.toString(),
                        commune2 = dialogBinding.etCommune2.text.toString()
                    )

                    // Save new profile to Firestore
                    db.collection("Profile").document(newProfileId).set(profile).await()

                    // Find existing node for the parent profile
                    val existingNodeQuery = db.collection("Node")
                        .whereEqualTo("id_profile", parentId)
                        .whereEqualTo("id_tree", Utility.treeId)
                        .get()
                        .await()

                    if (existingNodeQuery.isEmpty) {
                        // If no node exists, create a new one
                        val newNode = NodeDTO(
                            id = parentId,
                            id_tree = Utility.treeId,
                            id_profile = parentId,
                            id_partner = if (relationshipType == "partner") newProfileId else null,
                            id_children = if (relationshipType == "child") listOf(newProfileId) else null
                        )
                        db.collection("Node").document(parentId).set(newNode).await()
                    } else {
                        // If node exists, update it
                        val existingNode = existingNodeQuery.documents[0]
                        val existingNodeDTO = existingNode.toObject(NodeDTO::class.java)

                        if (relationshipType == "partner") {
                            if (existingNodeDTO?.id_partner != null) {
                                Toast.makeText(requireContext(), "Người này đã có vợ/chồng", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            db.collection("Node").document(existingNode.id).update("id_partner", newProfileId).await()
                        } else {
                            // Adding child
                            val updatedChildren = (existingNodeDTO?.id_children ?: emptyList()) + newProfileId
                            db.collection("Node").document(existingNode.id).update("id_children", updatedChildren).await()
                        }
                    }

                    Toast.makeText(requireContext(), "Thêm thành công", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadFamilyTree() // Reload the tree
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving profile", e)
                    Toast.makeText(requireContext(), "Có lỗi xảy ra khi lưu", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 