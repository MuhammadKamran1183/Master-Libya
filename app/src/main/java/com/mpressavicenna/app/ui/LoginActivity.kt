package com.mpressavicenna.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mpressavicenna.app.FCMListener
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.ActivityLoginBinding
import com.mpressavicenna.app.util.*
import com.mpressavicenna.app.util.Loading.cancelLoading
import com.mpressavicenna.app.util.Loading.showLoading
import io.paperdb.Paper

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.tvSignUp.setOnClickListener {
            openActivity(RegisterActivity::class.java, false)
        }

        binding.tvForgotPassword.setOnClickListener { forgotPassword() }

        binding.btnLoginActivity.setOnClickListener {
            if (binding.tilEmail.isEmailValid() && binding.tilPassword.isPasswordValid()){
                login(binding.tilEmail.value(true), binding.tilPassword.value())
            }
        }

    }

    private fun login(email: String, password: String) {

        if (!isOnline()) {
            displayPopUp(
                getString(R.string.error),
                getString(R.string.no_internet_access)
            )
            return
        }

        showLoading()
        Log.e("LoginActivity", "email: $email :; password: $password")
        Constant.mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                Log.e("LoginActivity", "login: $task")
                if (task.isSuccessful) {
                    Constant.mAuth.uid?.let { authId ->

                        Paper.book().write(Constant.k_authId, authId)

                        getUserData(object : GeneralListener {
                            override fun buttonClick(clicked: Boolean) {
                                if (clicked) {
                                    cancelLoading()
                                    Constant.rootRef.child(Constant.k_tableUser)
                                        .child(authId)
                                        .child("fcmToken")
                                        .setValue(FCMListener.getToken(this@LoginActivity))
                                    Paper.book().write(Constant.k_checkLogin, true)

                                    openActivity(HomeActivity::class.java)

                                }
                            }
                        })

                    }
                }
                else {
                    displayPopUp(
                        getString(R.string.error),
                        task.exception?.localizedMessage
                    )
                }
            }
    }

}