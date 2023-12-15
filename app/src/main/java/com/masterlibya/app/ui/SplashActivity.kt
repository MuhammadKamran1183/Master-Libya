package com.masterlibya.app.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.masterlibya.app.databinding.ActivitySplashBinding
import com.masterlibya.app.util.*
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var i = 0
    private var timer = Timer()
    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timer.schedule(3000) {
            openActivity(IntroActivity::class.java)
        }
    }

//        setHomeStatusBarColor(this)
//        getBaseUrl()
//
//        Constant.mListSocialLinks.clear()
//        Constant.mListSocialLinks.addAll(createSocialLinks())
//
//        if (Paper.book().read<Boolean>(Constant.k_checkLogin) == true) {
//            timer.schedule(2000) {
//                getUserData(object : GeneralListener {
//                    override fun buttonClick(clicked: Boolean) {
//                        super.buttonClick(clicked)
//                        openActivity(HomeActivity::class.java)
//
////                        if (Paper.book().read<User>(Constant.k_activeUser)?.tagUid.isNullOrEmpty()){
////                            openActivity(PodsActivity::class.java)
////                        }else{
////                            openActivity(HomeActivity::class.java)
////                        }
//                    }
//                })
//            }
//        } else {
//            timer.schedule(2000) {
//                openActivity(IntroActivity::class.java)
//            }
//        }
//
//    }
//
//    private fun createSocialLinks() = mutableListOf(
//        /*Phone*/
//        SocialLink(
//            linkID = 501,
//            name = getString(R.string.phone),
//            value = "",
//            socialIcon = drawable(R.drawable.ic_phone),
//            packageName = "",
//            baseUrl = "phone",
//            title = R.string.phone
//        ),
//        /*Instagram*/
//        SocialLink(
//            linkID = 103,
//            name = resources.getString(R.string.instagram),
//            socialIcon = drawable(R.drawable.ic_insta),
//            value = "",
//            packageName = "com.instagram.android",
//            baseUrl = "http://instagram.com/_u/",
//            title = R.string.instagram
//        ),
//        // Facebook
//        SocialLink(
//            linkID = 102,
//            name = resources.getString(R.string.facebook),
//            socialIcon = drawable(R.drawable.ic_facebook),
//            value = "",
//            packageName = "com.facebook.katana",
//            baseUrl = "",
//            title = R.string.facebook
//        ),
//        //TikTok
//        SocialLink(
//            linkID = 111,
//            name = resources.getString(R.string.tiktok),
//            socialIcon = drawable(R.drawable.ic_tiktok),
//            value = "",
//            packageName = "com.zhiliaoapp.musically",
//            baseUrl = "https://www.tiktok.com/@",
//            title = R.string.tiktok
//        ),
//        //Whatsapp
//        SocialLink(
//            linkID = 115,
//            name = resources.getString(R.string.whatsapp),
//            socialIcon = drawable(R.drawable.ic_whatsapp),
//            value = "",
//            packageName = "com.whatsapp",
//            baseUrl = "https://api.whatsapp.com/send?phone=",
//            title = R.string.whatsapp
//        ),
//        //LinkedIn
//        SocialLink(
//            linkID = 104,
//            name = resources.getString(R.string.linkedIn),
//            socialIcon = drawable(R.drawable.ic_linkedin),
//            value = "",
//            packageName = "com.linkedin.android",
//            baseUrl = "http://www.linkedin.com/profile/view?id=",
//            title = R.string.linkedIn
//        ),
//        //Telegram
//        SocialLink(
//            linkID = 109,
//            name = resources.getString(R.string.telegram),
//            socialIcon = drawable(R.drawable.ic_telegram),
//            value = "",
//            packageName = "org.telegram.messenger",
//            baseUrl = "https://t.me/",
//            title = R.string.telegram
//        ),
//        //Snapchat
//        SocialLink(
//            linkID = 107,
//            name = resources.getString(R.string.snapchat),
//            socialIcon = drawable(R.drawable.ic_snapchat),
//            value = "",
//            packageName = "com.snapchat.android",
//            baseUrl = "https://snapchat.com/add/",
//            title = R.string.snapchat
//        ),
//        //Twitter
//        SocialLink(
//            linkID = 112,
//            name = resources.getString(R.string.twitter),
//            socialIcon = drawable(R.drawable.ic_x_twitter),
//            value = "",
//            packageName = "com.twitter.android",
//            baseUrl = "https://twitter.com/#!/",
//            title = R.string.twitter
//        ),
//        //Website
//        SocialLink(
//            linkID = 114,
//            name = resources.getString(R.string.website),
//            socialIcon = drawable(R.drawable.ic_website),
//            value = "",
//            packageName = "",
//            baseUrl = "web",
//            title = R.string.website
//        ),
//        //Youtube
//        SocialLink(
//            linkID = 116,
//            name = resources.getString(R.string.youtube),
//            socialIcon = drawable(R.drawable.ic_youtube),
//            value = "",
//            packageName = "com.google.android.youtube",
//            baseUrl = "vnd.youtube:",
//            title = R.string.youtube
//        ),
//        //Spotify
//        SocialLink(
//            linkID = 108,
//            name = resources.getString(R.string.spotify),
//            socialIcon = drawable(R.drawable.ic_spotify),
//            value = "",
//            packageName = "com.spotify.music",
//            baseUrl = "",
//            title = R.string.spotify
//        ),
//        //Email
//        SocialLink(
//            linkID = 101,
//            name = resources.getString(R.string.email),
//            socialIcon = drawable(R.drawable.ic_email),
//            value = "",
//            packageName = "",
//            baseUrl = "email",
//            title = R.string.email
//        ),
//        //Paypal
//        SocialLink(
//            linkID = 105,
//            name = resources.getString(R.string.paypal),
//            socialIcon = drawable(R.drawable.ic_paypal),
//            value = "",
//            packageName = "com.paypal.android.p2pmobile",
//            baseUrl = "",
//            title = R.string.paypal
//        ),
//        //Pinterest
//        SocialLink(
//            linkID = 106,
//            name = resources.getString(R.string.pinterest),
//            socialIcon = drawable(R.drawable.ic_pinterest),
//            value = "",
//            packageName = "com.pinterest",
//            baseUrl = "https://www.pinterest.com/",
//            title = R.string.pinterest
//        ),
//        //Skype
//        SocialLink(
//            linkID = 500,
//            name = resources.getString(R.string.skype),
//            socialIcon = drawable(R.drawable.ic_skype),
//            value = "",
//            packageName = "com.skype.raider",
//            baseUrl = "",
//            title = R.string.skype
//        ),
//        //Calendly
//        SocialLink(
//            linkID = 117,
//            name = resources.getString(R.string.calendly),
//            socialIcon = drawable(R.drawable.ic_calendly),
//            value = "",
//            packageName = "com.calendly.app",
//            baseUrl = "",
//            title = R.string.calendly
//        ),
//        SocialLink(
//            name = resources.getString(R.string.custom_link_1),
//            socialIcon = drawable(R.drawable.ic_custom_link),
//            value = "",
//            packageName = "",
//            baseUrl = "web",
//            title = R.string.custom_link_1,
//            linkID = 50
//        ),
//        SocialLink(
//            name = resources.getString(R.string.custom_link_2),
//            socialIcon = drawable(R.drawable.ic_custom_link),
//            value = "",
//            packageName = "",
//            baseUrl = "web",
//            title = R.string.custom_link_2,
//            linkID = 51
//        ),
//        SocialLink(
//            name = resources.getString(R.string.custom_link_3),
//            socialIcon = drawable(R.drawable.ic_custom_link),
//            value = "",
//            packageName = "",
//            baseUrl = "web",
//            title = R.string.custom_link_3,
//            linkID = 52
//        )
//    )
//
//    private fun getBaseUrl() {
//        Constant.rootRef.child(Constant.k_tableBaseUrl).child("url")
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    Constant.k_SERVER_IP = snapshot.getValue(String::class.java)!!
//                    Paper.book()
//                        .write(Constant.k_baseUrl, "${snapshot.getValue(String::class.java)}")
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//            })
//    }
//
//    override fun onBackPressed() {
//        if (i == 0) {
//            i++
//            binding.root.snackBar("Press back again to exit.")
//        } else {
//            val a = Intent(Intent.ACTION_MAIN)
//            a.addCategory(Intent.CATEGORY_HOME)
//            a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(a)
//        }
//    }
//
//    override fun onPause() {
//        timer.cancel()
//        super.onPause()
//    }

}