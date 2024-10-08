package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log

class ScreenTimeReceiver : BroadcastReceiver() {

    private var screenOnStart: Long = 0L // Time when the screen was turned on
    private val thirtySecondsMillis: Long = 30 * 1000 // 30 seconds in milliseconds

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            // Screen turned on, start tracking time
            screenOnStart = SystemClock.elapsedRealtime()
            Log.d("ScreenTimeReceiver", "Screen turned on at $screenOnStart")
        } else if (intent.action == Intent.ACTION_SCREEN_OFF) {
            // Screen turned off, calculate elapsed time
            if (screenOnStart != 0L) {
                val elapsedTime = SystemClock.elapsedRealtime() - screenOnStart
                Log.d("ScreenTimeReceiver", "Screen turned off after ${elapsedTime / 1000} seconds")

                // Check if 30 seconds have passed to send a notification
                if (elapsedTime >= thirtySecondsMillis) {
                    sendNotification(context, "30 Seconds Screen Time", "You have been using your phone for 30 seconds. Time for a break!")
                    Log.d("ScreenTimeReceiver", "30 seconds notification sent.")
                }
                screenOnStart = 0L // Reset the start time
            }
        }
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        // Create an intent to start the NotificationService with the notification details
        val notificationIntent = Intent(context, NotificationService::class.java).apply {
            putExtra("notification_title", title)
            putExtra("notification_message", message)
        }
        context.startService(notificationIntent) // Start the service to send the notification
    }
}
