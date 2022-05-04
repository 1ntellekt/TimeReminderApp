package com.example.timereminderapp.receiver

import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.timereminderapp.R
import com.example.timereminderapp.models.CurrentUser
import com.example.timereminderapp.models.NoteTask
import kotlin.random.Random

class AlarmReceiver : BroadcastReceiver() {

    private val channelId = "myNotify"

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            val notificationId = Random.nextInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                createNotificationChannel(notificationManagerCompat)
            }

            val noteTask = intent!!.extras?.getSerializable("noteTask") as NoteTask

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigation_app)
                .setDestination(R.id.mainFragment)
                .createPendingIntent()

            val notification = NotificationCompat.Builder(context,channelId)
                .setStyle(NotificationCompat.BigTextStyle()
                    .setSummaryText("Напоминание")
                    .setBigContentTitle("Настала задача:${noteTask.name}!")
                    .bigText(noteTask.description))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_baseline_notifications_active)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            notificationManagerCompat.notify(notificationId,notification.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
        val channelName = "myChannel"
        val channel = NotificationChannel(channelId,channelName,IMPORTANCE_HIGH).apply {
            enableLights(true)
        }
        notificationManagerCompat.createNotificationChannel(channel)
    }


}