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

class EventsFragment : Fragment() {

    // Lazy initialization of views
    private val calendarView by lazy { requireView().findViewById<CalendarView>(R.id.calendar_view) }
    private val btnCreateEvent by lazy { requireView().findViewById<Button>(R.id.btn_create_event) }
    private val tvNoEvents by lazy { requireView().findViewById<TextView>(R.id.tv_no_events) }
    private val eventsList by lazy { requireView().findViewById<LinearLayout>(R.id.events_list) }
    private val btnBack by lazy { requireView().findViewById<ImageButton>(R.id.btn_back) }

    private lateinit var firestore: FirebaseFirestore
    private val eventDates = mutableMapOf<String, MutableList<EventDTO>>()
    private val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        setupEventsListener()
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

        // Display events for current date initially
        displayEventsForDate(dateFormat.format(Date()))
    }

    private fun setupEventsListener() {
        firestore.collection("events").addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }

            if (snapshots != null) {
                eventDates.clear() // Clear existing events before updating

                for (document in snapshots.documents) {
                    val eventDate = document.getString("eventDate")
                    val eventName = document.getString("name")

                    if (eventDate != null && eventName != null) {
                        val event = EventDTO(eventName, "", eventDate)
                        if (eventDates.containsKey(eventDate)) {
                            eventDates[eventDate]?.add(event)
                        } else {
                            eventDates[eventDate] = mutableListOf(event)
                        }
                    }
                }

                // Refresh the display for current date
                view?.let {
                    displayEventsForDate(dateFormat.format(Date()))
                }
            }
        }
    }

    // Display events for a specific date
    private fun displayEventsForDate(date: String) {
        eventDates[date]?.let { events ->
            if (events.isNotEmpty()) {
                tvNoEvents.visibility = View.GONE
                eventsList.visibility = View.VISIBLE
                eventsList.removeAllViews()

                events.forEach { event ->
                    layoutInflater.inflate(R.layout.item_event, eventsList, false).apply {
                        findViewById<TextView>(R.id.tv_event_name).text = event.name
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
