package com.masterlibya.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.masterlibya.app.databinding.ActivityOtpactivityBinding
import com.masterlibya.app.util.openActivity

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVerify.setOnClickListener {
            openActivity(RegisteredActivity::class.java)
        }
    }
}