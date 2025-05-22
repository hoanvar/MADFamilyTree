package com.dung.madfamilytree.dtos

import com.google.firebase.Timestamp


data class NotificationItem(
    val id: String = "",
    val fromName: String = "",
    val fromId: String = "",
    val timestamp: Timestamp,
    val type: String = "link_request",  // hoặc "event"
    val message: String = ""            // dùng cho thông báo sự kiện
)

