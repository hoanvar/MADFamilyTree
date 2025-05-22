package com.dung.madfamilytree.views.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dung.madfamilytree.R
import com.google.firebase.firestore.FirebaseFirestore
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
        tvNoNotifications = view.findViewById(R.id.tv_no_notifications)
        notificationsList = view.findViewById(R.id.notifications_list)

        // Lấy thông báo từ Firestore
        fetchEventNotifications()
    }

    private fun fetchEventNotifications() {
        val eventsRef = firestore.collection("events")

        // Lấy tất cả các tài liệu trong collection "events"
        eventsRef.get()
            .addOnSuccessListener { result ->
                // Kiểm tra nếu có dữ liệu
                if (!result.isEmpty) {
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
                    for ((eventDate, notificationText) in sortedEvents) {
                        addNotification(notificationText)
                    }

                } else {
                    tvNoNotifications.visibility = View.VISIBLE
                    notificationsList.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
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

        // Nếu thời gian còn lại là âm, tức là sự kiện đã qua, hiển thị thông báo sự kiện đã diễn ra
        if (diffInMillis < 0) {
            return ""  // Trả về chuỗi rỗng nếu sự kiện đã diễn ra
        }

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
            else -> "" // Không thông báo nếu xa hơn 1 tuần
        }
    }

    private fun addNotification(notification: String) {
        // Inflate item_notification layout và thêm vào notificationsList
        layoutInflater.inflate(R.layout.item_notification, notificationsList, false).apply {
            findViewById<TextView>(R.id.tv_notification_text).text = notification
            notificationsList.addView(this)
        }
    }
}
