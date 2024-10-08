package notivate.com

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log // Import the Log class

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "ScreenTimeChannel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // Create the notification channel
        createNotificationChannel(context)

        // Send the notification
        sendNotification(context)
    }

    private fun createNotificationChannel(context: Context) {
        // Check if the notification channel already exists
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Screen Time Notifications"
            val descriptionText = "Notifications for screen time tracking"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create the notification channel only if it hasn't been created before
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun sendNotification(context: Context) {
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app's icon
            .setContentTitle("Screen Time Alert")
            .setContentText("You've used your phone for another hour (1 minute for testing)!") // Custom message
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Check for notification permission and send the notification
        with(NotificationManagerCompat.from(context)) {
            // Check if the permission is granted
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Send the notification
                notify(NOTIFICATION_ID, notification)
            } else {
                // Log permission denial or handle accordingly
                // You can choose to log a message or notify the user
                Log.d("NotificationReceiver", "Notification permission not granted.")
            }
        }
    }
}