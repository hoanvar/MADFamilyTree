package com.dung.madfamilytree.views.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.AccountDTO
import com.dung.madfamilytree.dtos.ProfileDTO
import com.dung.madfamilytree.utility.Utility
import com.dung.madfamilytree.databinding.ActivityRegistorBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RegistorActivity : BaseActivity() {
    private lateinit var binding: ActivityRegistorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView10.setOnClickListener {
            finish()
        }

        binding.registorBtn.setOnClickListener {
            val fullName = binding.userFullName.text.toString()
            val username = binding.userName.text.toString()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            if (validateInput(fullName, username, password, confirmPassword)) {
                checkUsernameExists(username) { exists ->
                    if (exists) {
                        Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show()
                    } else {
                        createProfileAndRegister(fullName, username, password)
                    }
                }
            }
        }
    }

    private fun validateInput(
        fullName: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ và tên", Toast.LENGTH_SHORT).show()
            return false
        }
        if (username.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun checkUsernameExists(username: String, callback: (Boolean) -> Unit) {
        Utility.db?.collection("Account")
            ?.whereEqualTo("username", username)
            ?.get()
            ?.addOnSuccessListener { documents ->
                callback(documents.isEmpty.not())
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "Lỗi kiểm tra tên đăng nhập", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createProfileAndRegister(fullName: String, username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create profile first
                val profile = ProfileDTO(
                    name = fullName,
                    matched_recordsId_matched_record = null,
                    another_name = null,
                    gender = null,
                    date_of_birth = null,
                    phone_number = null,
                    marital_status = null,
                    educational_level = null,
                    job = null,
                    province1 = null,
                    district1 = null,
                    commune1 = null,
                    province2 = null,
                    district2 = null,
                    commune2 = null,
                    died = null
                )

                val profileId = createProfile(profile)
                if (profileId.isNotEmpty()) {
                    // Create account with profile ID
                    val account = AccountDTO(
                        id_profile = profileId,
                        username = username,
                        password = password,
                    )

                    val accountId = Utility.addAccount(account)
                    withContext(Dispatchers.Main) {
                        if (accountId.isNotEmpty()) {
                            Toast.makeText(this@RegistorActivity, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@RegistorActivity, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegistorActivity, "Tạo hồ sơ thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegistorActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun createProfile(profile: ProfileDTO): String {
        return suspendCancellableCoroutine { cont ->
            Utility.db?.collection("Profile")
                ?.add(profile)
                ?.addOnSuccessListener { documentReference ->
                    cont.resume(documentReference.id)
                }
                ?.addOnFailureListener { exception ->
                    cont.resume("")
                }
        }
    }
}