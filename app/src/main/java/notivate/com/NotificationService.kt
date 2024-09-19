package notivate.com

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat

class NotificationService : Service() {

    companion object {
        private const val CHANNEL_ID = "notification_channel"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_INTERVAL_MS = 60000L // 1 minute
    }

    private val handler = Handler()
    private val notificationTexts = listOf(
        "Reminder: Take a break!",
        "Don't forget to stretch!",
        "How about a quick walk?",
        "Time to rest your eyes!",
        "Stay hydrated!"
    )
    private var currentNotificationIndex = 0

    override fun onCreate() {
        super.onCreate()
        // Create notification channel for Android versions O and above
        createNotificationChannel()
        // Start sending notifications
        startSendingNotifications()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // We don't need to build a notification here since notifications are handled by the Handler
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

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

    private fun startSendingNotifications() {
        handler.post(object : Runnable {
            override fun run() {
                sendNotification()
                // Schedule the next notification
                handler.postDelayed(this, NOTIFICATION_INTERVAL_MS)
            }
        })
    }

    private fun sendNotification() {
        val title = "Notification Alert"
        val text = notificationTexts[currentNotificationIndex]
        currentNotificationIndex = (currentNotificationIndex + 1) % notificationTexts.size

        val notification = buildNotification(title, text)

        // Display the notification
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(title: String, text: String): Notification {
        val intent = Intent(this, NotificationClickActivity::class.java).apply {
            putExtra("notificationId", "test_notification_id")
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification) // Use your own notification icon here
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }
}
