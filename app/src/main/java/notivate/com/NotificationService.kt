package notivate.com

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class NotificationService : Service() {

    companion object {
        private const val CHANNEL_ID = "notification_channel"
        private const val NOTIFICATION_ID = 1
    }

    private val handler = Handler(Looper.getMainLooper())

    // Predefined intervals in milliseconds
    private val notificationIntervals = listOf(15000L, 30000L, 45000L, 60000L, 120000L)

    private val notificationTexts = listOf(
        "Reminder: Take a break!",
        "You have been on your phone for too long today.",
        "How about a quick walk?",
        "Time to rest your eyes!",
        "You've been on your phone for a while, take a break!"
    )

    private val notificationTitles = listOf(
        "Break Reminder",
        "Reminder",
        "Stretch Break",
        "Eyes Rest",
        "Health Alert"
    )

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startSendingNotifications() // Start sending notifications at random intervals
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

    private fun sendNotification() {
        val title = notificationTitles[Random.nextInt(notificationTitles.size)]
        val text = notificationTexts[Random.nextInt(notificationTexts.size)]

        val notification = buildNotification(title, text)

        // Update the notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        Log.d("NotificationService", "Notification sent: $text")
    }

    private fun buildNotification(title: String, text: String): Notification {
        val randomInterval = getRandomIntervalFromList()
        val notificationKey = logNotificationToFirebase(title, text, randomInterval) // Log interval too

        val clickIntent = Intent(this, NotificationClickActivity::class.java).apply {
            putExtra("notification_key", notificationKey) // Pass the key
        }

        val clickPendingIntent = PendingIntent.getActivity(
            this,
            0,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification) // Use your own notification icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(clickPendingIntent)  // Attach the PendingIntent
            .setAutoCancel(true)
            .build()
    }

    private fun startSendingNotifications() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                sendNotification()

                // Schedule the next notification with a random interval from the list
                val nextInterval = getRandomIntervalFromList()
                handler.postDelayed(this, nextInterval)
            }
        }, getRandomIntervalFromList()) // Delay the first notification with a random interval
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop sending notifications when service is destroyed
    }

    private fun getRandomIntervalFromList(): Long {
        return notificationIntervals[Random.nextInt(notificationIntervals.size)]
    }

    private fun logNotificationToFirebase(title: String, text: String, interval: Long): String? {
        val database = FirebaseDatabase.getInstance().getReference("notifications")
        val notificationData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "title" to title,
            "text" to text,
            "clicked" to false, // Initially set as not clicked
            "interval" to interval // Log the interval in milliseconds
        )

        // Push the notification data and get the unique key
        val notificationRef = database.push()
        notificationRef.setValue(notificationData)
            .addOnSuccessListener {
                Log.d("Firebase", "Notification data logged successfully with key: ${notificationRef.key}")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to log notification data: ${e.message}")
            }

        return notificationRef.key // Return the generated unique key
    }
}
