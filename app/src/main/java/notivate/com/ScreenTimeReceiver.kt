package notivate.com

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class ScreenTimeReceiver : BroadcastReceiver() {

    private var screenOnStart: Long = 0L
    private var totalScreenOnTime: Long = 0L
    private var nextNotificationTime: Long = 60 * 1000L // 1 minute for testing (change to 60 * 60 * 1000L for 1 hour)

    private val notificationTexts = listOf(
        "Reminder: Take a break!",
        "You have been on your phone for too long today.",
        "How about a quick walk?",
        "Time to rest your eyes!",
        "You've been on your phone for a while, take a break!"
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
        val title = "Reminder" // or any title you want for the notification
        val text = notificationTexts[currentNotificationIndex]
        currentNotificationIndex =(currentNotificationIndex + 1) % notificationTexts.size

        // Start the NotificationService using an Intent
        val notificationIntent = Intent(context, NotificationService::class.java).apply {
            putExtra("title", title)
            putExtra("text", text)
        }
        context.startService(notificationIntent)

        // Log notification data to Firebase (if needed)
        logNotificationToFirebase(context, title, text)
    }

    // Logs notification data to Firebase
    private fun logNotificationToFirebase(context: Context, title: String, text: String) {
        Log.d("ScreenTimeReceiver", "Logging notification to Firebase.")
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