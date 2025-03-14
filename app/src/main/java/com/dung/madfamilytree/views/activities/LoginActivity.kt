package com.dung.madfamilytree.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ActivityLoginBinding
import com.dung.madfamilytree.databinding.ActivityMainBinding

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpEvent()
    }
    fun setUpEvent(){
        binding.registorLink.setOnClickListener{
            startActivity(Intent(this@LoginActivity,RegistorActivity::class.java))

        }
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
        }
    }
}