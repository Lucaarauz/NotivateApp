package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class ScreenTimeReceiver : BroadcastReceiver() {

    private var screenOnStart: Long = 0L // Time when the screen was turned on
    private val oneHourMillis: Long = 5000 //3600 * 1000 // 1 hour in milliseconds

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            // Screen turned on, start tracking time
            screenOnStart = SystemClock.elapsedRealtime()
        } else if (intent.action == Intent.ACTION_SCREEN_OFF) {
            // Screen turned off, calculate and accumulate screen-on time
            if (screenOnStart != 0L) {
                val elapsedTime = SystemClock.elapsedRealtime() - screenOnStart
                if (elapsedTime >= oneHourMillis) {
                    sendNotification(context, "1 Hour Screen Time", "You have been using your phone for 1 hour. Time for a break!")
                }
                screenOnStart = 0L
            }
        }
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val notificationIntent = Intent(context, NotificationService::class.java).apply {
            putExtra("notification_title", title)
            putExtra("notification_message", message)
        }
        context.startService(notificationIntent)
    }
}
