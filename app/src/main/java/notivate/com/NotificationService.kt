package notivate.com

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.app.PendingIntent

class NotificationService : Service() {

    companion object {
        private const val CHANNEL_ID = "notification_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification(
            title = "Take a Break!",
            text = "You have been on your phone for 1 hour. It's time to take a break!"
        )

        startForeground(NOTIFICATION_ID, notification)

        // Stop the foreground service after showing the notification
        stopForeground(true)
        stopSelf()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
            val name = "Screen Time Reminder"
            val descriptionText = "Channel for sending screen time notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(title: String, text: String): Notification {
        // Create an Intent to launch an Activity when the notification is clicked
        val clickIntent = Intent(this, NotificationClickActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Attach the PendingIntent to the notification
            .build()
    }
}