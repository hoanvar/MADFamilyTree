package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.FirebaseFirestore
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.LinkRequests
import com.dung.madfamilytree.utility.Utility
import java.text.SimpleDateFormat
import java.util.*

class NotificationFragment : Fragment() {

    private lateinit var tvNoNotifications: TextView
    private lateinit var notificationsList: LinearLayout

    // Khởi tạo Firestore
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_notification, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo Firestore và UI
        firestore = FirebaseFirestore.getInstance()

        tvNoNotifications = view.findViewById(R.id.tvEmpty)
        notificationsList = view.findViewById(R.id.notificationsList)

// Lấy thông báo từ Firestore
        fetchEventNotifications()
        fetchLinkRequests()


    }
    private fun fetchLinkRequests() {
        val userId = Utility.accountId ?: return

        tvNoNotifications.visibility = View.GONE
        notificationsList.visibility = View.VISIBLE

        // Thêm event từ intent nếu có (ví dụ: từ FirebaseMessagingService)
        val eventMessage = arguments?.getString("event_message")
        if (!eventMessage.isNullOrBlank()) {
            val itemView = layoutInflater.inflate(R.layout.item_notification, notificationsList, false)
            itemView.findViewById<TextView>(R.id.tvMessage).text = eventMessage
            itemView.findViewById<TextView>(R.id.tvTime).text = "Vừa xong"
            notificationsList.addView(itemView)
        }

        // Load link requests
        firestore.collection("LinkRequests")
            .whereEqualTo("toId", userId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty && eventMessage.isNullOrBlank()) {
                    tvNoNotifications.visibility = View.VISIBLE
                    notificationsList.visibility = View.GONE
                    return@addOnSuccessListener
                }

                for (doc in result) {
                    val requestId = doc.id
                    val fromId = doc.getString("fromId") ?: continue
                    val fromName = doc.getString("fromName") ?: "Người dùng không rõ"
                    val timestamp = doc.getTimestamp("timestamp")

                    val message = "Bạn nhận được yêu cầu kết nối từ $fromName"

                    val itemView = layoutInflater.inflate(R.layout.item_notification, notificationsList, false)
                    itemView.findViewById<TextView>(R.id.tvMessage).text = message
                    itemView.findViewById<TextView>(R.id.tvTime).text = Utility.formatTimestamp(timestamp)

                    itemView.setOnClickListener {
                        val action = NotificationFragmentDirections
                            .actionNotificationFragmentToRequestDetailFragment(requestId, fromName, fromId)
                        findNavController().navigate(action)
                    }

                    notificationsList.addView(itemView)
                }
            }
    }


    private fun fetchEventNotifications() {
        val eventsRef = firestore.collection("events")

// Lấy tất cả các tài liệu trong collection "events"
        eventsRef.get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    // Kiểm tra nếu có dữ liệu
                    tvNoNotifications.visibility = View.GONE
                    notificationsList.visibility = View.VISIBLE

// Khởi tạo TreeMap để sắp xếp sự kiện theo ngày
                    val sortedEvents = sortedMapOf<Date, String>()

// Duyệt qua các tài liệu trong Firestore
                    result.forEach { document ->
                        val eventName = document.getString("name")
                        val eventDate = document.getString("eventDate")

                        eventName?.let { name ->
                            eventDate?.let { date ->
                                // Chuyển chuỗi eventDate thành đối tượng Date
                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val eventDateObj = formatter.parse(date)

                                // Kiểm tra nếu sự kiện đã diễn ra, bỏ qua nếu đã qua
                                if (eventDateObj != null) {
                                    val notificationText = generateNotificationText(name, date)

                                    // Nếu sự kiện chưa diễn ra (text không rỗng), thêm vào TreeMap
                                    if (notificationText.isNotEmpty()) {
                                        sortedEvents[eventDateObj] = notificationText
                                    }
                                }
                            }
                        }
                    }
                    // Thêm các sự kiện đã sắp xếp vào danh sách thông báo
                    for ((_, notificationText) in sortedEvents) {
                        addNotification(notificationText)
                    }
                }
            }
            .addOnFailureListener {
                tvNoNotifications.text = "Error loading notifications"
                tvNoNotifications.visibility = View.VISIBLE
                notificationsList.visibility = View.GONE
            }
    }

    private fun generateNotificationText(eventName: String, eventDate: String): String {
        // Đổi chuỗi eventDate thành Date object
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val eventDateObj = formatter.parse(eventDate)
        val currentDate = Date()

// Tính số ngày còn lại
        val diffInMillis = eventDateObj.time - currentDate.time
        if (diffInMillis < 0) return ""

// Nếu thời gian còn lại là âm, tức là sự kiện đã qua, hiển thị thông báo sự kiện đã diễn ra
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        val diffInHours = (diffInMillis / (1000 * 60 * 60)).toInt() % 24
        val diffInMinutes = (diffInMillis / (1000 * 60)).toInt() % 60

        return when {
            diffInMinutes <= 15 -> "Chỉ còn 15 phút nữa đến sự kiện: $eventName"
            diffInMinutes <= 30 -> "Chỉ còn 30 phút nữa đến sự kiện: $eventName"
            diffInHours == 1 -> "Chỉ còn 1 giờ nữa đến sự kiện: $eventName"
            diffInHours <= 4 -> "Còn $diffInHours giờ nữa đến sự kiện: $eventName"
            diffInDays == 1 -> "Còn 1 ngày nữa đến sự kiện: $eventName"
            diffInDays <= 3 -> "Còn $diffInDays ngày nữa đến sự kiện: $eventName"
            diffInDays <= 7 -> "Sắp đến sự kiện: $eventName (còn $diffInDays ngày)"
            else -> ""
        }
    }

    private fun addNotification(notification: String) {
        // Inflate item_notification layout và thêm vào notificationsList
        layoutInflater.inflate(R.layout.item_notification, notificationsList, false).apply {
            findViewById<TextView>(R.id.tvMessage).text = notification
            notificationsList.addView(this)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchLinkRequests()
        fetchEventNotifications()
    }

}
