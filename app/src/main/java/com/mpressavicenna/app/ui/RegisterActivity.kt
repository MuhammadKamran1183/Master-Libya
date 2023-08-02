package com.mpressavicenna.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mpressavicenna.app.FCMListener
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.ActivityRegisterBinding
import com.mpressavicenna.app.model.User
import com.mpressavicenna.app.util.*
import com.mpressavicenna.app.util.Loading.cancelLoading
import com.mpressavicenna.app.util.Loading.showLoading
import io.paperdb.Paper

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val userData = User()
    var socialRegister = false
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras.let {
            if (it?.getString("id")?.isNotEmpty() == true) {
                userData.id = it.getString("id")
                binding.tilPassword.visibility = View.GONE
            }
            if (it?.getString("name")?.isNotEmpty() == true) {
                userData.name = it.getString("name")!!
                binding.tilName.editText?.text = it.getString("name")!!.toEditable()
                socialRegister = true
            }
            if (it?.getString("email")?.isNotEmpty() == true) {
                userData.email = it.getString("email")
                binding.tilEmail.editText?.text = it.getString("email")!!.toEditable()
            }
            if (it?.getString("photoUrl")?.isNotEmpty() == true) {
                userData.profileUrl = it.getString("photoUrl")!!
            }
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.btnRegister.setOnClickListener { validation() }

        binding.tvPrivacyPolicy.setOnClickListener {
            openActivity(TermsAndPrivacy::class.java, false) { putString("key", "privacy") }
        }

        binding.tvTermsAndConditions.setOnClickListener {
            openActivity(TermsAndPrivacy::class.java, false) { putString("key", "terms") }
        }

    }

    private fun validation() {

        binding.tilName.error = null
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilPhone.error = null

        if (binding.tilName.validate() && binding.tilUsername.validate() &&
            binding.tilPhone.validate() && binding.tilEmail.isEmailValid()
        ) {

            if (!socialRegister) {
                binding.tilPassword.isPasswordValid()
                return
            }

            userData.email = binding.tilEmail.value(true)
            userData.username = binding.tilUsername.value(true)
            userData.name = binding.tilName.value()
            userData.phone = binding.tilPhone.value()
            userData.fcmToken = FCMListener.getToken(this)!!
            checkUsernameExistence()
        }

    }

    private fun checkUsernameExistence() {
        showLoading()
        val isUserNameExist: Query =
            Constant.rootRef.child(Constant.k_tableUser).orderByChild("username")
                .equalTo(binding.tilUsername.value(true))

        isUserNameExist.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    if (socialRegister) {
                        socialRegister()
                    } else {
                        registerUser(
                            binding.tilEmail.value(true),
                            binding.tilPassword.value()
                        )
                    }
                } else {
                    cancelLoading()
                    displayPopUp(
                        getString(R.string.error),
                        getString(R.string.username_already_exist)
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                cancelLoading()
                displayPopUp(
                    getString(R.string.error),
                    error.message
                )
            }
        })
    }

    private fun registerUser(email: String, password: String) {

        if (!isOnline()) {
            displayPopUp(
                getString(R.string.error),
                getString(R.string.no_internet_access)
            )
            return
        }

        showLoading()
        Constant.mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    Constant.mAuth.uid?.let { authId ->

                        userData.id = authId
                        userData.parentId = authId
                        Paper.book().write(Constant.k_authId, authId)

                        Constant.rootRef.child(Constant.k_tableUser)
                            .child(authId)
                            .setValue(userData)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    displayPopUp(
                                        getString(R.string.success),
                                        getString(R.string.user_registered_successful),
                                        object : GeneralListener {
                                            override fun buttonClick(clicked: Boolean) {
                                                cancelLoading()
                                                Paper.book()
                                                    .write(Constant.k_activeUser, userData)
                                                Paper.book().write(Constant.k_checkLogin, true)
//                                                openActivity(PurchaseActivity::class.java)
                                                openActivity(HomeActivity::class.java)
                                            }
                                        }
                                    )
                                } else {
                                    displayPopUp(
                                        getString(R.string.error),
                                        it.exception?.message
                                    )
                                }
                            }
                    }

                } else {
                    displayPopUp(
                        getString(R.string.error),
                        task.exception?.message
                    )
                }
            }
    }

    private fun socialRegister() {
        db = FirebaseDatabase.getInstance()
        db.reference.child(Constant.k_tableUser).child(userData.id!!).setValue(userData)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    this.displayPopUp(
                        getString(R.string.success),
                        "User Registered Successfully",
                        object : GeneralListener {
                            override fun buttonClick(clicked: Boolean) {
                                super.buttonClick(clicked)
                                if (clicked) {
                                    openActivity(HomeActivity::class.java)
                                }
                            }

                        }
                    )

                } else {
                    this.displayPopUp(
                        getString(R.string.error),
                        it.exception?.message
                    )
                }
            }
    }

}