package com.dung.madfamilytree.dtos

data class LinkRequests(
    val fromId: String = "",      // ID người gửi yêu cầu liên kết
    val fromName: String = "",     // Tên người gửi yêu cầu
    val toId: String = "",        // ID người nhận (hiện đang đăng nhập)
    val status: String = "pending", // Trạng thái: "pending", "success", "declined"
    val timestamp: com.google.firebase.Timestamp? = null // Thời điểm gửi
)
