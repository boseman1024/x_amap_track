package com.axisx.x_amap_track_example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity: FlutterActivity() {
    private val CHANNEL_ID_SERVICE_RUNNING = "CHANNEL_ID_SERVICE_RUNNING"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        //createNotification()
    }

    private fun createNotification() {
        val builder: Notification.Builder
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID_SERVICE_RUNNING, "app service", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
            builder = Notification.Builder(applicationContext, CHANNEL_ID_SERVICE_RUNNING)
        } else {
            builder = Notification.Builder(applicationContext)
        }
        val nfIntent = Intent(this@MainActivity, MainActivity::class.java)
        nfIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        builder.setContentIntent(PendingIntent.getActivity(this@MainActivity, 0, nfIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("后台运行中")
                .setContentText("后台运行中")
        val notification = builder.build()
        nm.notify(1, notification)
    }
}
