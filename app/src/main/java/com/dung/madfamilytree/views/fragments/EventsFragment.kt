package com.dung.madfamilytree.views.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.EventDTO
import com.dung.madfamilytree.views.activities.CreateEventActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.set

class EventsFragment : Fragment() {

    // Lazy initialization of views
    private val calendarView by lazy { requireView().findViewById<CalendarView>(R.id.calendar_view) }
    private val btnCreateEvent by lazy { requireView().findViewById<Button>(R.id.btn_create_event) }
    private val tvNoEvents by lazy { requireView().findViewById<TextView>(R.id.tv_no_events) }
    private val eventsList by lazy { requireView().findViewById<LinearLayout>(R.id.events_list) }
    private val btnBack by lazy { requireView().findViewById<ImageButton>(R.id.btn_back) }

    private lateinit var firestore: FirebaseFirestore
    private val eventDates = mutableMapOf<String, MutableList<EventDTO>>() // To store events by date
    private val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()


        // Fetch events from Firestore
        fetchEventsFromFirestore()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_events, container, false)



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up create event button
        btnCreateEvent.setOnClickListener {
            Intent(requireContext(), CreateEventActivity::class.java).also {
                startActivity(it)
            }
        }

        // Set up calendar view listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
                val selectedDate = dateFormat.format(time)
                displayEventsForDate(selectedDate)
            }
        }
        btnBack.setOnClickListener {
            // Navigate back
            requireActivity().onBackPressed()
        }

        listenedEventUpdate();
        // Display events for current date initially
        displayEventsForDate(dateFormat.format(Date()))
    }
    private fun listenedEventUpdate(){
        val db = FirebaseFirestore.getInstance()

// Lắng nghe thay đổi trong collection "dates"
        val datesRef = db.collection("events")

// Thêm snapshot listener để lắng nghe sự thay đổi (bao gồm cả document mới)
        datesRef.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }

            if (snapshots != null) {
                // Duyệt qua các document trong snapshot
                for (document in snapshots.documentChanges) {
                    if (document.type == DocumentChange.Type.ADDED) {
                        // Nếu là document mới được thêm vào
                        val eventDate = document.document.getString("eventDate") as String // Get event date
                        val eventName = document.document.getString("name") as String // Get event name
                        val event = EventDTO(eventName, "", eventDate)
                        // Xử lý dữ liệu document mới, ví dụ cập nhật UI
                        eventDate.let {
                            // Cập nhật UI hoặc làm gì đó với dữ liệu
                            if (eventDates.containsKey(eventDate)) {
                                eventDates[eventDate]?.add(event)
                            } else {
                                eventDates[eventDate] = mutableListOf(event)
                            }
                        }
                    }
                }
            }
        }

    }

    // Fetch events from Firestore and update eventDates
    private fun fetchEventsFromFirestore() {
        firestore.collection("events") // Firestore collection "events"
            .get() // Get all documents
            .addOnSuccessListener { result ->
                val fetchedEvents = mutableMapOf<String, MutableList<EventDTO>>()

                // Loop through Firestore documents and parse event data
                for (document in result) {
                    val eventDate = document.getString("eventDate") // Get event date
                    val eventName = document.getString("name") // Get event name

                    if (eventDate != null && eventName != null) {
                        val event = EventDTO(eventName, "", eventDate) // Create Event object (no notification)

                        // Add event to the map based on eventDate
                        if (fetchedEvents.containsKey(eventDate)) {
                            fetchedEvents[eventDate]?.add(event)
                        } else {
                            fetchedEvents[eventDate] = mutableListOf(event)
                        }
                    }
                }

                // Update eventDates with fetched events
                eventDates.clear()
                eventDates.putAll(fetchedEvents)

                // Display events for today's date after fetching from Firestore
                displayEventsForDate(dateFormat.format(Date()))
            }
    }

    // Display events for a specific date
    private fun displayEventsForDate(date: String) {
        // Check if there are any events for the selected date
        eventDates[date]?.let { events ->
            if (events.isNotEmpty()) {
                tvNoEvents.visibility = View.GONE
                eventsList.visibility = View.VISIBLE
                eventsList.removeAllViews() // Clear old events

                // Add each event to the layout
                events.forEach { event ->
                    layoutInflater.inflate(R.layout.item_event, eventsList, false).apply {
                        findViewById<TextView>(R.id.tv_event_name).text = event.name
//                        findViewById<TextView>(R.id.tv_event_date).text = event.eventDate
                        eventsList.addView(this)
                    }
                }
            } else {
                showNoEvents()
            }
        } ?: showNoEvents() // If no events for that date, show "No events"
    }

    // Show "No events" message when there are no events for the selected date
    private fun showNoEvents() {
        tvNoEvents.visibility = View.VISIBLE
        eventsList.visibility = View.GONE
    }
}
