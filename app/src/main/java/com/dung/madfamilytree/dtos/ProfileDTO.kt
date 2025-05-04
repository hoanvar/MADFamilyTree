package com.dung.madfamilytree.dtos

data class ProfileDTO(
    val matched_recordsId_matched_record: Int?,
    val name: String?,
    val another_name: String?,
    val gender: String?,
    val date_of_birth: String?,
    val phone_number: String?,
    val marital_status: String?,
    val educational_level: String?,
    val job: String?,
    val province1: String?,
    val district1: String?,
    val commune1: String?,
    val province2: String?,
    val district2: String?,
    val commune2: String?,
    val died: Int?
)