package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.SystemClock

class ScreenTimeReceiver : BroadcastReceiver() {

    private var screenOnStart: Long = 0L // Time when the screen was turned on
    private val oneMinuteMillis: Long = 60 * 1000 // 1 minute in milliseconds
    private val oneHourMillis: Long = 3600 * 1000 // 1 hour in milliseconds
    private val twoHourMillis: Long = 2 * 3600 * 1000 // 2 hours in milliseconds

    override fun onReceive(context: Context, intent: Intent) {
        val preferences = context.getSharedPreferences("screen_time_prefs", Context.MODE_PRIVATE)
        val screenOnTime = preferences.getLong("screen_on_time", 0L) // Total screen-on time

        if (intent.action == Intent.ACTION_SCREEN_ON) {
            // Screen turned on, start tracking time
            screenOnStart = SystemClock.elapsedRealtime()
        } else if (intent.action == Intent.ACTION_SCREEN_OFF) {
            // Screen turned off, calculate and accumulate screen-on time
            if (screenOnStart != 0L) {
                val elapsedTime = SystemClock.elapsedRealtime() - screenOnStart
                val newScreenOnTime = screenOnTime + elapsedTime

                // Save accumulated screen-on time
                with(preferences.edit()) {
                    putLong("screen_on_time", newScreenOnTime)
                    apply()
                }

                screenOnStart = 0L

                // Check for time intervals and send notifications
                checkTimeIntervals(context, newScreenOnTime, preferences)
            }
        }
    }

    private fun checkTimeIntervals(context: Context, screenOnTime: Long, preferences: SharedPreferences) {
        val notificationSent = preferences.getBoolean("notification_sent", false)

        if (screenOnTime >= twoHourMillis && !notificationSent) {
            sendNotification(context, "2 Hour Screen Time", "You have been using your phone for 2 hours. Time for a break!")
            resetScreenTime(preferences)
        } else if (screenOnTime >= oneHourMillis && !notificationSent) {
            sendNotification(context, "1 Hour Screen Time", "You have been using your phone for 1 hour. Take a break!")
            resetScreenTime(preferences)
        } else if (screenOnTime >= oneMinuteMillis && !notificationSent) {
            sendNotification(context, "1 Minute Screen Time", "You have been using your phone for 1 minute. Just a heads-up!")
            resetScreenTime(preferences)
        }
    }

    private fun resetScreenTime(preferences: SharedPreferences) {
        with(preferences.edit()) {
            putLong("screen_on_time", 0L)
            putBoolean("notification_sent", true)
            apply()
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