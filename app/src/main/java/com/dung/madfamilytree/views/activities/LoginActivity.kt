package com.dung.madfamilytree.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityLoginBinding
import com.dung.madfamilytree.databinding.ActivityMainBinding
import com.dung.madfamilytree.utility.Utility
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpEvent()
    }

    fun setUpEvent(){
//        binding.registorLink.setOnClickListener{
//            startActivity(Intent(this@LoginActivity,RegistorActivity::class.java))
//        }

        binding.loginBtn.setOnClickListener {
            val username = binding.userName.text.toString()
            val password = binding.password.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkLogin(username, password)
        }
    }

    private fun checkLogin(username: String, password: String) {
        Utility.db?.collection("Account")
            ?.whereEqualTo("username", username)
            ?.whereEqualTo("password", password)
            ?.get()
            ?.addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                } else {
                    // Lưu accountId vào Utility để sử dụng sau này
                    Utility.accountId = documents.documents[0].id
                    val accountData = documents.documents[0].data
                    accountData?.let { data ->
                        val treeId = accountData["tree_id"] as? String
                        if (treeId != "") {
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@LoginActivity, HomeNotInTree::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                    }

                    finish()
                }
            }
            ?.addOnFailureListener { exception ->
                Toast.makeText(this, "Lỗi đăng nhập: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}