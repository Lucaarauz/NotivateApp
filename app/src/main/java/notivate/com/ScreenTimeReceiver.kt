package notivate.com

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log

class ScreenTimeReceiver : BroadcastReceiver() {
    private var screenOnStart: Long = 0L
    private var totalScreenOnTime: Long = 0L
    private var nextNotificationTime: Long = 60 * 1000L // 1 minute for testing (change to 60 * 60 * 1000L for 1 hour)

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

    private var currentNotificationIndex = 0


    init {
        Log.d("ScreenTimeReceiver", "ScreenTimeReceiver initialized.")
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                Log.d("ScreenTimeReceiver", "Screen ON action received.")
                screenOnStart = SystemClock.elapsedRealtime()
                Log.d("ScreenTimeReceiver", "Screen turned on at $screenOnStart")

                // Schedule notification check every 10 seconds while the screen is on
                scheduleNotificationCheck(context)
            }
            Intent.ACTION_SCREEN_OFF -> {
                Log.d("ScreenTimeReceiver", "Screen OFF action received.")
                if (screenOnStart != 0L) {
                    // Calculate time the screen was on during this session
                    val elapsedTime = SystemClock.elapsedRealtime() - screenOnStart
                    totalScreenOnTime += elapsedTime
                    Log.d("ScreenTimeReceiver", "Screen turned off after ${elapsedTime / 1000} seconds, total time = ${totalScreenOnTime / 1000} seconds")
                    screenOnStart = 0L

                    // Cancel any scheduled notifications when the screen turns off
                    cancelNotificationCheck(context)
                }
            }
        }
    }

    // Schedules a check for sending notifications
    private fun scheduleNotificationCheck(context: Context) {
        Log.d("ScreenTimeReceiver", "Scheduling notification check.")

        // Check if it's time to send a notification
        if (totalScreenOnTime >= nextNotificationTime) {
            Log.d("ScreenTimeReceiver", "It's time to send a notification.")
            sendNotification(context)

            // Update the next notification time to the next minute (or hour for production)
            nextNotificationTime += 60 * 1000L // Add 1 minute for testing, or 60 * 60 * 1000L for 1 hour
            Log.d("ScreenTimeReceiver", "Next notification time set to ${nextNotificationTime / 1000} seconds.")
        } else {
            Log.d("ScreenTimeReceiver", "Not yet time for the next notification.")
        }

        // Schedule a check every 10 seconds
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val checkIntent = Intent(context, ScreenTimeReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, checkIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 10 * 1000L, // Check every 10 seconds
            pendingIntent
        )
        Log.d("ScreenTimeReceiver", "Notification check scheduled for 10 seconds from now.")
    }

    // Cancels the notification check when the screen is off
    private fun cancelNotificationCheck(context: Context) {
        Log.d("ScreenTimeReceiver", "Cancelling notification scheduling.")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val checkIntent = Intent(context, ScreenTimeReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, checkIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
        Log.d("ScreenTimeReceiver", "Notification scheduling canceled.")
    }

    // Sends the notification using the method from MainActivity
    private fun sendNotification(context: Context) {
        val title = "Reminder"
        val text = notificationTexts[currentNotificationIndex]
        currentNotificationIndex =(currentNotificationIndex + 1) % notificationTexts.size

        // Start the NotificationService using an Intent
        val notificationIntent = Intent(context, NotificationService::class.java).apply {
            putExtra("title", title)
            putExtra("text", text)
        }
        context.startService(notificationIntent)
    }
}