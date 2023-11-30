package com.masterlibya.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.masterlibya.app.databinding.ActivityRegisteredBinding
import com.masterlibya.app.util.openActivity

class RegisteredActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisteredBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisteredBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener { openActivity(LoginActivity::class.java, false) }
    }
}