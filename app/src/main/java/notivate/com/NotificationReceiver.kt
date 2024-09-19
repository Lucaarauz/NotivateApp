package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Extract notification title and message from intent extras
        val title = intent.getStringExtra("notification_title") ?: "Reminder"
        val message = intent.getStringExtra("notification_message") ?: "Take a break!"

        // Create a new intent to start the NotificationService
        val serviceIntent = Intent(context, NotificationService::class.java).apply {
            putExtra("notification_title", title)
            putExtra("notification_message", message)
        }

        // Start the NotificationService with the provided title and message
        context.startService(serviceIntent)
    }
}
