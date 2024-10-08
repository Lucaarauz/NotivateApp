package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log

class ScreenTimeReceiver : BroadcastReceiver() {

    private var screenOnStart: Long = 0L

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            screenOnStart = SystemClock.elapsedRealtime()
            Log.d("ScreenTimeReceiver", "Screen turned on at $screenOnStart")

            // Start NotificationService to send notifications every 30 seconds
            val serviceIntent = Intent(context, NotificationService::class.java)
            context.startService(serviceIntent) // Start the NotificationService
        } else if (intent.action == Intent.ACTION_SCREEN_OFF) {
            if (screenOnStart != 0L) {
                val elapsedTime = SystemClock.elapsedRealtime() - screenOnStart
                Log.d("ScreenTimeReceiver", "Screen turned off after ${elapsedTime / 1000} seconds")
                screenOnStart = 0L

                // Optionally, stop the NotificationService if you want to stop notifications when the screen is off
                val serviceIntent = Intent(context, NotificationService::class.java)
                context.stopService(serviceIntent) // Stop the NotificationService
            }
        }
    }
}
