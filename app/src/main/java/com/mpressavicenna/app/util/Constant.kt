package com.mpressavicenna.app.util

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.mpressavicenna.app.model.SocialLink

object Constant {

    var k_SERVER_IP = "https://profile.mypress.com/"
    var k_SHOP_IP = "http://tappze.link2avicenna.com/"
    var k_PRIVACY_POLICY = "http://tappze.link2avicenna.com/"
    var k_TERMS_OF_USE = "http://tappze.link2avicenna.com/"

    internal const val k_InAppKey =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAugQ53WqnV3RhOuA/hZZjx9tNwBC8WOR5NkxolvD7JViXnqARkwbFd5+WpAM2WMgBznHpeqFVF/s53mUxlqYLIZD+uSQKAz4IGrbYCzDJC6rm/+GvvSgZd5hJxZwP/ub7HA+Yvge2bV6b8To9+Cpmv33n4AR4M0flRwvCjGtiH+BbEod6llvq92/ttRLWieu6+HaJygsfwQEbHAdFFBuAfMqpuSqItbN914wsn3CBy6UDtYhYi9/fey4WEzdYIovKqm4eR3QmTEFYesCeV61jWxoR8WRtPwULCmdU/H2IPoxUn/9YfUOfLEOate1VLksw5331qP4Fh8diOwiqZsoLlwIDAQAB"
    internal const val k_checkLogin = "checkLogin"
    internal const val k_userProfiles = "userProfiles"
    internal const val k_activeUser = "activeUser"
    internal const val k_baseUrl = "baseUrl"
    internal const val k_authId = "userAuthId"
    internal const val k_selectedLanguage = "selectedLanguage"

    //FirebaseRootRef
    internal val rootRef: DatabaseReference = Firebase.database.reference
    internal val mAuth: FirebaseAuth = Firebase.auth
    internal val mStorage: StorageReference = Firebase.storage.reference

    //FirebaseDatabaseTables
    internal const val k_tableUser = "Users"
    internal const val k_tableAnalytic = "Analytic"
    internal const val k_tableContact = "Contacts"
    internal const val k_tableSocialLinks = "SocialLinks"
    internal const val k_tableBaseUrl = "BaseUrl"

    //ObserveNfcTag
    var k_nfcDevice = false
    val k_newIntent = MutableLiveData<Intent?>()

    //SocialLinksList
    internal val mListSocialLinks = mutableListOf<SocialLink>()

}