package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.NodeDTO
import com.dung.madfamilytree.dtos.ProfileDTO
import com.dung.madfamilytree.utility.Utility
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TreeFragment : Fragment() {
    private val TAG = "TreeFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_tree, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        val treeId = Utility.treeId
        Log.d(TAG, "TreeId: $treeId")

        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(TAG, "Starting to fetch family tree data")
            val (nodes, profiles) = fetchFamilyTree(treeId)
            Log.d(TAG, "Fetched nodes count: ${nodes?.size}")
            Log.d(TAG, "Fetched profiles count: ${profiles.size}")
            
            nodes?.let {
                val rootId = Utility.rootId
                Log.d(TAG, "RootId: $rootId")
                val treeRoot = buildTree(rootId, it, profiles)
                Log.d(TAG, "Tree built successfully: ${treeRoot != null}")
                printTreeToLog(treeRoot)
            } ?: run {
                Log.e(TAG, "No nodes found for treeId: $treeId")
            }
        }

        setupObservers()
    }

    private fun setupObservers() {
        Log.d(TAG, "setupObservers called")
    }

    data class TreeNode(
        val profileId: String,
        val profile: ProfileDTO?,
        val partner: TreeNode? = null,
        val children: MutableList<TreeNode> = mutableListOf()
    )

    suspend fun fetchFamilyTree(treeId: String): Pair<List<NodeDTO>?, MutableMap<String, ProfileDTO>> {
        Log.d(TAG, "fetchFamilyTree started for treeId: $treeId")
        val nodes = Utility.db?.collection("Node")
            ?.whereEqualTo("id_tree", treeId)
            ?.get()?.await()
            ?.mapNotNull { it.toObject(NodeDTO::class.java) }
        
        Log.d(TAG, "Raw nodes fetched: ${nodes?.size}")
        val snapshot = Utility.db?.collection("Node")
            ?.whereEqualTo("id_tree", treeId)
            ?.get()?.await()

        snapshot?.forEach {
            Log.d(TAG, "Raw Node document: ${it.data}")
        }

        // Truy xuất tất cả profile liên quan
        val profileIds = nodes?.flatMap { listOfNotNull(it.id_profile, it.id_partner) + (it.id_children ?: emptyList()) }
            ?.filterNotNull()
            ?.toSet()
        logAllNodeIds(nodes)
        
        Log.d(TAG, "Profile IDs to fetch: ${profileIds?.size}")

        val profiles = mutableMapOf<String, ProfileDTO>()
        profileIds?.chunked(10)?.forEach { batch ->
            val batchProfiles = Utility.db?.collection("Profile")
                ?.whereIn(FieldPath.documentId(), batch)
                ?.get()?.await()
                ?.map { doc -> doc.id to doc.toObject(ProfileDTO::class.java) }
            
            Log.d(TAG, "Fetched batch of profiles: ${batchProfiles?.size}")
            batchProfiles?.forEach { (id, profile) -> profiles[id] = profile }
        }

        return Pair(nodes, profiles)
    }

    fun buildTree(rootId: String, nodes: List<NodeDTO>, profiles: Map<String, ProfileDTO>): TreeNode? {
        Log.d(TAG, "buildTree started for rootId: $rootId")

        val nodeMap = nodes.associateBy { it.id_profile }
        val visited = mutableSetOf<String>()

        fun helper(id: String?): TreeNode? {
            if (id == null || visited.contains(id)) return null
            visited.add(id)

            val profile = profiles[id] ?: return null
            val node = nodeMap[id]

            // Partner chỉ thêm vào nếu tồn tại, không đệ quy partner
            val partnerId = node?.id_partner
            val partnerProfile = profiles[partnerId]

            val partnerNode = if (partnerId != null && !visited.contains(partnerId)) {
                visited.add(partnerId)
                TreeNode(
                    profileId = partnerId,
                    profile = partnerProfile
                )
            } else null

            // Con cái
            val children = node?.id_children?.mapNotNull { helper(it) }?.toMutableList() ?: mutableListOf()

            return TreeNode(
                profileId = id,
                profile = profile,
                partner = partnerNode,
                children = children
            )
        }

        return helper(rootId)
    }


    fun printTreeToLog(root: TreeNode?, indent: String = "") {
        if (root == null) {
            Log.d(TAG, "printTreeToLog: root is null")
            return
        }

        val name = root.profile?.name ?: "Unknown"
        Log.d(TAG, "$indent👤 $name (ID: ${root.profileId})")

        root.partner?.let { partner ->
            val partnerName = partner.profile?.name ?: "Unknown"
            Log.d(TAG, "$indent   💍 Partner: $partnerName (ID: ${partner.profileId})")
        }

        root.children.forEach { child ->
            printTreeToLog(child, indent + "   ")
        }
    }

    private fun logAllNodeIds(nodes: List<NodeDTO>?) {
        if (nodes.isNullOrEmpty()) {
            Log.d(TAG, "No nodes to display.")
            return
        }

        Log.d(TAG, "==== Node IDs Detail ====")
        nodes.forEachIndexed { index, node ->
            Log.d(TAG, "Node[$index] -, id_profile: ${node.id_profile}, id_partner: ${node.id_partner}, id_children: ${node.id_children}")
        }
        Log.d(TAG, "==========================")
    }


} 