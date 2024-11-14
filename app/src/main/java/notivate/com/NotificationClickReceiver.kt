package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class NotificationClickReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "NotificationClickReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // Retrieve the notification ID and click status
        val notificationId = intent?.getIntExtra("notification_id", -1) ?: -1
        val clicked = intent?.getBooleanExtra("clicked", false) ?: false

        // Print a log message to confirm the click event was received
        Log.d(TAG, "Notification clicked! ID: $notificationId, Clicked: $clicked")

        if (notificationId != -1) {
            // Log the click event to Firebase
            logNotificationClickToFirebase(notificationId, clicked)
        }
    }

    private fun logNotificationClickToFirebase(notificationId: Int, clicked: Boolean) {
        val database = FirebaseDatabase.getInstance().getReference("notifications")
        val notificationClickData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "notification_id" to notificationId,
            "clicked" to clicked
        )

        // Push the data to Firebase
        database.push().setValue(notificationClickData)
            .addOnSuccessListener {
                Log.d(TAG, "Notification click logged successfully.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to log notification click: ${e.message}")
            }
    }
}
