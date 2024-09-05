package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Create an intent to start the NotificationService
        val serviceIntent = Intent(context, NotificationService::class.java).apply {
            // Optionally, pass extras if needed by the service
            putExtra("channelId", intent.getStringExtra("channelId"))
            putExtra("notificationId", intent.getIntExtra("notificationId", 0))
        }
        // Start the NotificationService
        context.startService(serviceIntent)
    }
}