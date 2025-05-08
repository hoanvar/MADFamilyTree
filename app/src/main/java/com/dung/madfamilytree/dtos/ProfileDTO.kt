package com.dung.madfamilytree.dtos

import com.google.firebase.Timestamp

data class ProfileDTO(
    val id : String ?= null,
    val matched_recordsId_matched_record: Int? = null,
    val name: String? = null,
    val another_name: String? = null,
    val gender: String? = null,
    val date_of_birth: Timestamp? = null,
    val phone_number: String? = null,
    val marital_status: String? = null,
    val educational_level: String? = null,
    val job: String? = null,
    val province1: String? = null,
    val district1: String? = null,
    val commune1: String? = null,
    val province2: String? = null,
    val district2: String? = null,
    val commune2: String? = null,
    val died: Int? = null
)