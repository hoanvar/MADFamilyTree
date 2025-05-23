package com.dung.madfamilytree.views.activities


import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.EventDTO
import com.google.firebase.firestore.FirebaseFirestore

class CreateEventActivity : AppCompatActivity() {

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val etEventName by lazy { findViewById<EditText>(R.id.et_event_name) }
    private val spinnerNotification by lazy { findViewById<Spinner>(R.id.spinner_notification) }
    private val calendarView by lazy { findViewById<CalendarView>(R.id.calendar_view) }
    private val btnSave by lazy { findViewById<Button>(R.id.btn_save) }
    private val event by lazy { EventDTO() }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var pickedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        // Khởi tạo Firestore
        firestore = FirebaseFirestore.getInstance()

        setupToolbar()
        setupNotificationSpinner()
        setupSaveButton()
        setupCalendarView()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Tạo sự kiện"
        }
    }

    private fun setupNotificationSpinner() {
        val notificationOptions = arrayOf(
            "Không thông báo",
            "1 ngày trước",
            "3 ngày trước",
            "7 ngày trước"
        )

        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            notificationOptions
        ).also { adapter ->
            spinnerNotification.adapter = adapter
        }
    }

    private fun setupCalendarView() {
        // Lắng nghe sự kiện khi người dùng chọn ngày
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

            pickedDate=selectedDate
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val eventName = etEventName.text.toString()
            val notificationOption = spinnerNotification.selectedItem.toString()
            val eventDate = pickedDate

            if (eventName.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập tên sự kiện", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (eventDate.isBlank()) {
                Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val event = EventDTO(eventName, notificationOption, pickedDate)

            firestore.collection("events")
                .add(event)
                .addOnSuccessListener {
                    Toast.makeText(this, "Đã lưu sự kiện thành công", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lưu thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
