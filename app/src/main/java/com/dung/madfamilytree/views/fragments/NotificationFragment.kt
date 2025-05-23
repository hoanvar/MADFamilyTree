package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dung.madfamilytree.R
import com.dung.madfamilytree.utility.Utility
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificationFragment : Fragment() {

    private lateinit var tvNoNotifications: TextView
    private lateinit var notificationsList: LinearLayout
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_notification, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        tvNoNotifications = view.findViewById(R.id.tvEmpty)
        notificationsList = view.findViewById(R.id.notificationsList)
        setupFirestoreListeners()
    }

    private fun setupFirestoreListeners() {
        val userId = Utility.accountId ?: return

        firestore.collection("LinkRequests")
            .whereEqualTo("toId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { _, _ ->
                refreshNotifications()
            }
    }

    private fun refreshNotifications() {
        notificationsList.removeAllViews()
        val userId = Utility.accountId ?: return

        tvNoNotifications.visibility = View.GONE
        notificationsList.visibility = View.VISIBLE

        val eventMessage = arguments?.getString("event_message")
        if (!eventMessage.isNullOrBlank()) {
            addNotification(eventMessage, "Vừa xong")
        }

        firestore.collection("LinkRequests")
            .whereEqualTo("toId", userId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                var hasNotifications = !result.isEmpty || !eventMessage.isNullOrBlank()

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
                firestore.collection("events")
                    .get()
                    .addOnSuccessListener { eventsResult ->
                        val sortedEvents = sortedMapOf<Date, MutableList<String>>()

                        eventsResult.forEach { document ->
                            val eventName = document.getString("name")
                            val eventDate = document.getString("eventDate")

                            eventName?.let { name ->
                                eventDate?.let { date ->
                                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val eventDateObj = formatter.parse(date)

                                    if (eventDateObj != null) {
                                        val notificationText = generateNotificationText(name, date)
                                        if (notificationText.isNotEmpty()) {
                                            val list = sortedEvents.getOrPut(eventDateObj) { mutableListOf() }
                                            list.add(notificationText)
                                            hasNotifications = true
                                        }
                                    }
                                }
                            }
                        }

                        for ((_, notifications) in sortedEvents) {
                            for (notificationText in notifications) {
                                addNotification(notificationText, "")
                            }
                        }

                        tvNoNotifications.visibility = if (hasNotifications) View.GONE else View.VISIBLE
                        notificationsList.visibility = if (hasNotifications) View.VISIBLE else View.GONE
                    }
            }
    }

    private fun generateNotificationText(eventName: String, eventDate: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val eventDateObj = formatter.parse(eventDate)

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayDate = todayCalendar.time

        val eventCalendar = Calendar.getInstance().apply {
            time = eventDateObj
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val eventDateOnly = eventCalendar.time

        val diffInMillis = eventDateOnly.time - todayDate.time
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

        return when (diffInDays) {
            0 -> "Sự kiện $eventName diễn ra hôm nay!"
            1 -> "Còn 1 ngày nữa đến sự kiện: $eventName"
            3 -> "Còn 3 ngày nữa đến sự kiện: $eventName"
            7 -> "Còn 7 ngày nữa đến sự kiện: $eventName"
            else -> ""
        }
    }

    private fun addNotification(message: String, time: String = "") {
        val itemView = layoutInflater.inflate(R.layout.item_notification, notificationsList, false)
        itemView.findViewById<TextView>(R.id.tvMessage).text = message
        if (time.isNotEmpty()) {
            itemView.findViewById<TextView>(R.id.tvTime).text = time
        }
        notificationsList.addView(itemView)
    }

    override fun onResume() {
        super.onResume()
        refreshNotifications()
    }
}
