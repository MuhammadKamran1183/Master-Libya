package com.masterlibya.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.masterlibya.app.ui.SplashActivity
import kotlin.random.Random

class FCMListener : FirebaseMessagingService() {

    companion object {
        private val TAG = FCMListener::class.java.simpleName

        fun getToken(context: Context): String? {
            return context.getSharedPreferences("_", MODE_PRIVATE).getString("fcm_token", "empty")
        }
    }

    override fun onNewToken(p0: String) {
        Log.e(TAG, "onNewToken: $p0")
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fcm_token", p0).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.e(TAG, "notification: ${remoteMessage.notification}")
        Log.e(TAG, "body: ${remoteMessage.data}")

        val intent = Intent(this, SplashActivity::class.java)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )
        val notification = NotificationCompat.Builder(this, getString(R.string.channel_id))
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["body"])
            .setSmallIcon(R.drawable.ic_logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)

        super.onMessageReceived(remoteMessage)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        notificationManager: NotificationManager
    ) {
        val channel =
            NotificationChannel(
                getString(R.string.channel_id),
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_name)
                enableLights(true)
                lightColor = Color.WHITE
            }
        notificationManager.createNotificationChannel(channel)
    }

}