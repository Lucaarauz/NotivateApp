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
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class NotificationService : Service() {

    companion object {
        private const val CHANNEL_ID = "notification_channel"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_INTERVAL_MS = 30000L // 30 seconds
    }

    private val handler = Handler()
    private val notificationTexts = listOf(
        "Reminder: Take a break!",
        "You have been on your phone for too long today.",
        "How about a quick walk?",
        "Time to rest your eyes!",
        "You've been on your phone for a while, take a break!",
        "You deserve a break! Step away for a moment.",
        "Stretch out, refresh your body!",
        "Consider taking a few minutes for yourself.",
        "Your phone break is long overdue, go for a walk!",
        "Eyes tired? Time for a short pause.",
        "Too much screen time! Time to recharge.",
        "A quick break will do wonders for your focus!",
        "Step outside for a few minutes, you'll feel better!",
        "Break time! Close your eyes for a minute.",
        "How about a quick stretch to refresh?",
        "Your body and mind need a break. Take one now!",
        "A little rest goes a long way. Go for it!",
        "Take a breather – you’ll feel recharged!",
        "Step away from the screen and take a walk!",
        "How about a 5-minute stretch? It'll help!",
        "Why not grab some fresh air for a while?",
        "Time to hydrate and step away for a bit.",
        "You’ve earned a quick break, take it now!",
        "Your eyes will thank you for a short break.",
        "Feel the burn in your legs, take a walk now!",
        "You’re doing great, but a break will help you do better!",
        "It’s been a while! Take a quick walk outside.",
        "Take a moment to meditate. You’ve got this!",
        "Refuel with a healthy snack or drink. Refresh yourself!",
        "It's time to rest! Stand up and move around.",
        "Take a 5-minute break, it will boost your energy!",
        "Stop for a second. A break will help your productivity.",
        "Stuck on something? Take a break, your mind will reset!",
        "You’ve been working hard, take a moment to pause!",
        "Staring at the screen too long? Time for a breather!",
        "Quick 5-minute break, let’s refresh!",
        "Feeling tired? Maybe it's time for a stretch.",
        "How about walking to another room? It’ll help clear your mind.",
        "Click here to track your break time and reset your focus!",
        "Your eyes need a break – take a few minutes now.",
        "Take a short break and hydrate. Your body will thank you!",
        "Time for a rest – your focus will improve after a break!",
        "Get up, stretch, and get moving! Your mind will feel fresher!",
        "Pause and reset. Take a moment for yourself.",
        "Staring at the screen too long? It’s time for a break!"
    )

    private val notificationTitles = listOf(
        "Break Reminder",
        "Reminder",
        "Stretch Break",
        "Eyes Rest",
        "Health Alert"
    )

    private var currentNotificationIndex = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Start sending notifications at regular intervals
        startSendingNotifications()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // You can add additional logic here if needed
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
        val text = notificationTexts[currentNotificationIndex]
        currentNotificationIndex = (currentNotificationIndex + 1) % notificationTexts.size

        val notification = buildNotification(title, text)

        // Update the notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        Log.d("NotificationService", "Notification sent: $text")
    }

    private fun buildNotification(title: String, text: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        logNotificationToFirebase(
            title = title,
            text = text
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification) // Use your own notification icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun startSendingNotifications() {
        handler.post(object : Runnable {
            override fun run() {
                sendNotification()
                handler.postDelayed(this, NOTIFICATION_INTERVAL_MS)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop sending notifications when service is destroyed
    }

    private fun logNotificationToFirebase(title: String, text: String) {
        val database = FirebaseDatabase.getInstance().getReference("notifications")
        val notificationData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "title" to title,
            "text" to text
        )

        database.push().setValue(notificationData)
            .addOnSuccessListener {
                Log.d("Firebase", "Notification data logged successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to log notification data: ${e.message}")
            }
    }
}
