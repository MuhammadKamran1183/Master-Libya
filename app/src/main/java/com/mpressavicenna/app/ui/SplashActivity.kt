package com.mpressavicenna.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.ActivitySplashBinding
import com.mpressavicenna.app.model.SocialLink
import com.mpressavicenna.app.util.*
import io.paperdb.Paper
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

        if (Paper.book().read<Boolean>(Constant.k_checkLogin) == true) {
            getUserData(object : GeneralListener {
                override fun buttonClick(clicked: Boolean) {
                    if (clicked) {
                        getBaseUrl()

                        openActivity(HomeActivity::class.java)

                        /*if (Paper.book().read<User>(Constant.k_activeUser)?.isSubscribed == true &&
                            Paper.book().read<User>(Constant.k_activeUser)?.isCardPurchased == true){
                            openActivity(HomeActivity::class.java)
                        } else {
                            openActivity(PurchaseActivity::class.java)
                        }*/

                    } else {
                        openActivity(IntroActivity::class.java)
                    }
                }
            })
        } else {
            timer.schedule(2000) {
                openActivity(IntroActivity::class.java)
            }
        }

        Constant.mListSocialLinks.clear()
        Constant.mListSocialLinks.addAll(createSocialLinks())

    }

    private fun createSocialLinks() = mutableListOf(
        /*Phone*/
        SocialLink(
            name = getString(R.string.phone),
            socialIcon = drawable(R.drawable.ic_phone),
            value = "",
            packageName = "",
            baseUrl = "phone",
            title = R.string.phone
        ),
        /*Instagram*/
        SocialLink(
            name = resources.getString(R.string.instagram),
            socialIcon = drawable(R.drawable.ic_insta),
            value = "",
            packageName = "com.instagram.android",
            baseUrl = "http://instagram.com/_u/",
            title = R.string.instagram
        ),
        /*Facebook*/
        SocialLink(
            name = resources.getString(R.string.facebook),
            socialIcon = drawable(R.drawable.ic_facebook),
            value = "",
            packageName = "com.facebook.katana",
            baseUrl = "",
            title = R.string.facebook
        ),
        /*TikTok*/
        SocialLink(
            name = resources.getString(R.string.tiktok),
            socialIcon = drawable(R.drawable.ic_tiktok),
            value = "",
            packageName = "com.zhiliaoapp.musically",
            baseUrl = "https://www.tiktok.com/@",
            title = R.string.tiktok
        ),
        /*Whatsapp*/
        SocialLink(
            name = resources.getString(R.string.whatsapp),
            socialIcon = drawable(R.drawable.ic_whatsapp),
            value = "",
            packageName = "com.whatsapp",
            baseUrl = "https://api.whatsapp.com/send?phone=",
            title = R.string.whatsapp
        ),
        /*LinkedIn*/
        SocialLink(
            name = resources.getString(R.string.linkedIn),
            socialIcon = drawable(R.drawable.ic_linkedin),
            value = "",
            packageName = "com.linkedin.android",
            baseUrl = "http://www.linkedin.com/profile/view?id=",
            title = R.string.linkedIn
        ),
        /*Telegram*/
        SocialLink(
            name = resources.getString(R.string.telegram),
            socialIcon = drawable(R.drawable.ic_telegram),
            value = "",
            packageName = "org.telegram.messenger",
            baseUrl = "https://t.me/",
            title = R.string.telegram
        ),
        /*Snapchat*/
        SocialLink(
            name = resources.getString(R.string.snapchat),
            socialIcon = drawable(R.drawable.ic_snapchat),
            value = "",
            packageName = "com.snapchat.android",
            baseUrl = "https://snapchat.com/add/",
            title = R.string.snapchat
        ),
        /*Twitter*/
        SocialLink(
            name = resources.getString(R.string.twitter),
            socialIcon = drawable(R.drawable.ic_twitter),
            value = "",
            packageName = "com.twitter.android",
            baseUrl = "https://twitter.com/#!/",
            title = R.string.twitter
        ),
        /*Website*/
        SocialLink(
            name = resources.getString(R.string.website),
            socialIcon = drawable(R.drawable.ic_website),
            value = "",
            packageName = "",
            baseUrl = "web",
            title = R.string.website
        ),
        /*Youtube*/
        SocialLink(
            name = resources.getString(R.string.youtube),
            socialIcon = drawable(R.drawable.ic_youtube),
            value = "",
            packageName = "com.google.android.youtube",
            baseUrl = "vnd.youtube:",
            title = R.string.youtube
        ),
        /*Spotify*/
        SocialLink(
            name = resources.getString(R.string.spotify),
            socialIcon = drawable(R.drawable.ic_spotify),
            value = "",
            packageName = "com.spotify.music",
            baseUrl = "",
            title = R.string.spotify
        ),
        /*Email*/
        SocialLink(
            name = resources.getString(R.string.email),
            socialIcon = drawable(R.drawable.ic_email),
            value = "",
            packageName = "",
            baseUrl = "email",
            title = R.string.email
        ),
        /*Paypal*/
        SocialLink(
            name = resources.getString(R.string.paypal),
            socialIcon = drawable(R.drawable.ic_paypal),
            value = "",
            packageName = "com.paypal.android.p2pmobile",
            baseUrl = "",
            title = R.string.paypal
        ),
        /*Pinterest*/
        SocialLink(
            name = resources.getString(R.string.pinterest),
            socialIcon = drawable(R.drawable.ic_pinterest),
            value = "",
            packageName = "com.pinterest",
            baseUrl = "https://www.pinterest.com/",
            title = R.string.pinterest
        ),
        /*Skype*/
        SocialLink(
            name = resources.getString(R.string.skype),
            socialIcon = drawable(R.drawable.ic_skype),
            value = "",
            packageName = "com.skype.raider",
            baseUrl = "",
            title = R.string.skype
        ),
        /*Calendly*/
        SocialLink(
            name = resources.getString(R.string.calendly),
            socialIcon = drawable(R.drawable.ic_calendly),
            value = "",
            packageName = "com.calendly.app",
            baseUrl = "",
            title = R.string.calendly
        )
    )

    private fun getBaseUrl() {
        Constant.rootRef.child(Constant.k_tableBaseUrl).child("url")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Constant.k_SERVER_IP = snapshot.getValue(String::class.java)!!
                    Paper.book()
                        .write(Constant.k_baseUrl, "${snapshot.getValue(String::class.java)}")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onBackPressed() {
        if (i == 0) {
            i++
            binding.root.snackBar("Press back again to exit.")
        } else {
            val a = Intent(Intent.ACTION_MAIN)
            a.addCategory(Intent.CATEGORY_HOME)
            a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(a)
        }
    }

    override fun onPause() {
        timer.cancel()
        super.onPause()
    }

}