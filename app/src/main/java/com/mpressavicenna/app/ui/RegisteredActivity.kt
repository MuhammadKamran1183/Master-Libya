package com.mpressavicenna.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.ActivityRegisterBinding
import com.mpressavicenna.app.databinding.ActivityRegisteredBinding
import com.mpressavicenna.app.util.openActivity

class RegisteredActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisteredBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisteredBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener { openActivity(SplashActivity::class.java) }
    }
}