import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import notivate.com.R

class NotificationService : Service() {

    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "my_notification_channel" // Define your notification channel ID

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.getBooleanExtra("send_now", false)) {
            sendNotification()
        }
        return START_STICKY
    }

    private fun sendNotification() {
        // Check if the notification channel exists, if not, create it
        createNotificationChannel()

        // Check for the notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Create and send the notification
                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Test Notification")
                    .setContentText("This notification was sent from NotificationService!")
                    .setSmallIcon(R.drawable.ic_notification) // Change to your notification icon
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

                // Send the notification
                with(NotificationManagerCompat.from(this)) {
                    notify(NOTIFICATION_ID, notification)
                }

                Log.d("NotificationService", "Notification sent successfully.")
            } else {
                Log.d("NotificationService", "Notification permission not granted.")
            }
        } else {
            // For devices below Android 13, send the notification without checking
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Test Notification")
                .setContentText("This notification was sent from NotificationService!")
                .setSmallIcon(R.drawable.ic_notification) // Change to your notification icon
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            // Send the notification
            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, notification)
            }

            Log.d("NotificationService", "Notification sent successfully (below Android 13).")
        }
    }

    private fun createNotificationChannel() {
        val name = "My Notification Channel"
        val descriptionText = "Channel for app notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        // Register the channel with the system
        val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        Log.d("NotificationService", "Notification channel created.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}