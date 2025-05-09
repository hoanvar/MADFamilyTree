package com.dung.madfamilytree.dtos

data class District(
    val name: String,
    val code: Int,
    val wards: List<Ward>
)


