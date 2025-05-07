package com.dung.madfamilytree.dtos

data class NodeDTO(
    val id_tree: String? = null,
    val id_profile: String? = null,
    val id_partner: String? = null,
    val id_children: List<String?>? = null,
)
