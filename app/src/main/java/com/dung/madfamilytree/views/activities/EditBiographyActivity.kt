package com.dung.madfamilytree.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dung.madfamilytree.R
import com.dung.madfamilytree.utility.Utility
import com.google.firebase.firestore.FirebaseFirestore

class EditBiographyActivity : AppCompatActivity() {

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val etBiography by lazy { findViewById<EditText>(R.id.et_biography) }
    private val btnSave by lazy { findViewById<Button>(R.id.btn_save) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_biography)

        setupToolbar()
        setupSaveButton()

        // ✅ Lấy tiểu sử hiện tại
        val currentBio = intent.getStringExtra("currentBiography") ?: ""
        etBiography.setText(currentBio)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Soạn thảo tiểu sử"
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val biography = etBiography.text.toString()

            // ✅ Trả kết quả về ProfileCardFragment
            val resultIntent = Intent().apply {
                putExtra("updatedBiography", biography)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
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
