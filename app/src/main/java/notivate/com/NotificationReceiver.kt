package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Start the NotificationService to show the notification
        val serviceIntent = Intent(context, NotificationService::class.java)
        context.startService(serviceIntent)
    }
}