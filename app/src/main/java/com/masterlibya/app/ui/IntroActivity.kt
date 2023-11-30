package com.masterlibya.app.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.facebook.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.masterlibya.app.databinding.ActivityIntroBinding
import com.masterlibya.app.model.User
import com.masterlibya.app.util.Constant
import com.masterlibya.app.util.Loading.cancelLoading
import com.masterlibya.app.util.Loading.showLoading
import com.masterlibya.app.util.displayPopUp
import com.masterlibya.app.util.openActivity
import com.masterlibya.app.util.toast
import io.paperdb.Paper

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnManualEnter.setOnClickListener {
            openActivity(PodsActivity::class.java, false)
        }
        binding.btnGoogle.setOnClickListener {
            googleSignIn()
        }

        auth = Firebase.auth
//
////        binding.btnCreateAccount.setOnClickListener {
////            openActivity(RegisterActivity::class.java, false)
////        }
//
//        binding.tvSignIn.setOnClickListener {
//            openActivity(LoginActivity::class.java, false)
//        }
//
//        binding.btnGoogle.setOnClickListener {
//            googleSignIn()
//        }
//
////        binding.btnFacebook.setOnClickListener {
////            facebookSignIn()
////            binding.loginButton.performClick()
////        }
//
//    }
//
//    private fun facebookSignIn() {
//
//        val profile = Profile.getCurrentProfile()
//        if (profile != null) {
//            LoginManager.getInstance().logOut()
//        }
//
//        callbackManager = CallbackManager.Factory.create()
//
//        val permissionNeeds: List<String> = listOf("email", "public_profile")
////        binding.loginButton.setPermissions(permissionNeeds)
////
////        binding.loginButton.registerCallback(
////            callbackManager,
////            object : FacebookCallback<LoginResult?> {
////
////                override fun onSuccess(result: LoginResult?) {
////                    //Log.e(TAG, "onSuccess: $result")
////                    handleFacebookAccessToken(result?.accessToken!!)
////                    /*val request = GraphRequest.newMeRequest(
////                        AccessToken.getCurrentAccessToken()
////                    ) { obj, response ->
////                        try {
////                            Log.e(TAG, "object: $obj\nresponse: $response")
////                            val email = obj!!.getString("email")
////                            val name = obj!!.getString("name")
////                            val id = obj!!.getLong("id")
////                            val profilePicture = "https://graph.facebook.com/${obj.getString("id")}/picture?width=400&height=400"
//////                            handleFacebookAccessToken(result?.accessToken, name, email, image)
////
////                        } catch (e: JSONException) {
////                            e.printStackTrace()
////                        }
////                    }
////                    val parameters = Bundle()
////                    parameters.putString("fields", "id,email,name")
////                    request.parameters = parameters
////                    request.executeAsync()*/
////                }
////
////                override fun onCancel() {
////                    Toast.makeText(this@IntroActivity, "Login Cancelled", Toast.LENGTH_SHORT).show()
////                }
////
////                override fun onError(error: FacebookException) {
////                    //Log.e(TAG, "onError: ${error.message}")
////                }
////
////            })
//
//    }
//
//    private fun handleFacebookAccessToken(
//        token: AccessToken
//    ) {
//        showLoading()
//        //Log.d(TAG, "handleFacebookAccessToken:$token")
//
//        val credential = FacebookAuthProvider.getCredential(token.token)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    user?.let {
//                        //Log.e(TAG, "user: ${user.displayName} :: ${user.email}")
//                        checkUser(user)
//                    }
//                } else {
//                    cancelLoading()
//                    displayPopUp(
//                        getString(R.string.error),
//                        getString(R.string.you_already_registered_with_email)
//                    )
//
//                    /*Toast.makeText(
//                        baseContext, "Email already exist.",
//                        Toast.LENGTH_SHORT
//                    ).show()*/
//                }
//            }
//    }
//
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    //Log.e(TAG, "signInResult:failed code=" + e.statusCode)
                }
            }
        }

    private fun googleSignIn() {
        val account = GoogleSignIn.getLastSignedInAccount(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1093255451746-gpgnthskfupgav4gbpincl0lo1bn2828.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = mGoogleSignInClient.signInIntent

        if (account != null) {
            mGoogleSignInClient.signOut().addOnSuccessListener {
                googleSignInLauncher.launch(signInIntent)
            }
        } else {
            googleSignInLauncher.launch(signInIntent)
        }

    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        showLoading()
        //Log.e(TAG, "firebaseAuthWithGoogle: ")
        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        toast { "Login Success" }
                        openActivity(HomeActivity::class.java)
//                            checkUser(user)
                    }
                } else {
                    cancelLoading()
                    //Log.e(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun checkUser(user: FirebaseUser) {
        val isUserNameExist: Query =
            Constant.rootRef.child(Constant.k_tableUser).orderByChild("id")
                .equalTo(user.uid)

        isUserNameExist.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (!snapshot.exists()) {


                    val intent = Intent(this@IntroActivity, RegisterActivity::class.java)
                    intent.putExtra("id", user.uid)
                    intent.putExtra("name", user.displayName)
                    intent.putExtra("email", user.email)
                    intent.putExtra("photoUrl", user.photoUrl?.toString())
                    startActivity(intent)
                } else {

                    Constant.rootRef.child(Constant.k_tableUser)
                        .child(Constant.mAuth.uid!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    try {
                                        snapshot.getValue(User::class.java)?.let {
                                            Paper.book().write(Constant.k_activeUser, it)
//                                                saveCheckedLinks()
                                            Paper.book()
                                                .write(Constant.k_authId, Constant.mAuth.uid!!)
                                            Paper.book().write(Constant.k_checkLogin, true)
                                            openActivity(HomeActivity::class.java)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })

                }

            }

            override fun onCancelled(error: DatabaseError) {
                displayPopUp(
                    "Error",
                    error.message
                )
            }
        })
    }
}