package notivate.com

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class NotificationService : Service() {

    companion object {
        private const val CHANNEL_ID = "notification_channel"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_INTERVAL_MS = 20000 // 20 seconds
    }

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            sendNotification()
            handler.postDelayed(this, NOTIFICATION_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        handler.post(runnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Notification Service")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.ic_notification) // Replace with your icon
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun sendNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Scheduled Notification")
            .setContentText("This is a periodic notification.")
            .setSmallIcon(R.drawable.ic_notification) // Replace with your icon
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Channel"
            val descriptionText = "Channel for sending notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
