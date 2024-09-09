// BroadcastReceiver will listen for screen on and off events. Track the total screen-on time and trigger a notification once the user has spent 1 hour using the screen.

package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class ScreenTimeReceiver : BroadcastReceiver() {
    private var screenOnTime: Long = 0L // Total screen-on time in milliseconds
    private var screenOnStart: Long = 0L // Time when the screen was turned on
    private val oneHourMillis: Long = 60 * 60 * 1000 // 1 hour in milliseconds

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            // Screen turned on, start tracking time
            screenOnStart = SystemClock.elapsedRealtime() // Get current uptime
        } else if (intent.action == Intent.ACTION_SCREEN_OFF) {
            // Screen turned off, calculate and accumulate screen-on time
            if (screenOnStart != 0L) {
                screenOnTime += SystemClock.elapsedRealtime() - screenOnStart
                screenOnStart = 0L
            }

            // Check if screen-on time exceeds 1 hour
            if (screenOnTime >= oneHourMillis) {
                // Trigger notification to remind user to get off the phone
                val notificationIntent = Intent(context, NotificationReceiver::class.java)
                context.sendBroadcast(notificationIntent)
                screenOnTime = 0L // Reset the screen-on time after sending the notification
            }
        }
    }
}
