package com.dung.madfamilytree.dtos

public final data class Province(
    val name: String,
    val code: Int,
    val districts: List<District>
)

