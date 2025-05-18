package com.dung.madfamilytree.utility

import android.util.Log
import com.dung.madfamilytree.dtos.NodeDTO
import com.dung.madfamilytree.dtos.ProfileDTO
import com.dung.madfamilytree.dtos.TreeNode
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.tasks.await

class TreeUtility {
    companion object {
        private const val TAG = "TreeUtility"
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

                // Con cái
                val children = node?.id_children?.mapNotNull { helper(it) }?.toMutableList() ?: mutableListOf()

                val partnerNode = if (partnerId != null && !visited.contains(partnerId)) {
                    visited.add(partnerId)
                    TreeNode(
                        profileId = partnerId,
                        profile = partnerProfile,
                        children = children // Copy children to partner node
                    )
                } else null

                return TreeNode(
                    profileId = id,
                    profile = profile,
                    partner = partnerNode,
                    children = children
                )
            }

            val root = helper(rootId)
            printTreeToLog(root)
            return root
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

        fun groupProfilesByDepth(root: TreeNode?): Map<Int, List<TreeNode>> {
            val result = mutableMapOf<Int, MutableList<TreeNode>>()
            fun dfs(node: TreeNode?, depth: Int) {
                if (node == null) return
                result.getOrPut(depth) { mutableListOf() }.add(node)
                node.partner?.let { result.getOrPut(depth) { mutableListOf() }.add(it) }
                node.children.forEach { dfs(it, depth + 1) }
            }
            dfs(root, 0)
            return result
        }
    }
} 