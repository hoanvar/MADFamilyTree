package com.dung.madfamilytree.dtos

data class TreeNode(
    val profileId: String,
    val profile: ProfileDTO?,
    val partner: TreeNode? = null,
    val children: MutableList<TreeNode> = mutableListOf()
)
