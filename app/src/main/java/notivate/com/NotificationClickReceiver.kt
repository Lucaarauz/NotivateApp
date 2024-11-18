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
        Log.d(TAG, "onReceive triggered") // Log to check if the receiver is triggered

        // Retrieve the notification ID from the intent
        val notificationId = intent?.getStringExtra("notification_id") // Firebase UID
        if (notificationId == null) {
            Log.e(TAG, "Notification ID is null. Cannot track click event.")
            return
        }

        Log.d(TAG, "Notification clicked! Firebase ID: $notificationId")

        // Update Firebase to mark the notification as clicked
        logNotificationClickToFirebase(notificationId)
    }

    private fun logNotificationClickToFirebase(notificationId: String) {
        val database = FirebaseDatabase.getInstance().getReference("notifications").child(notificationId)

        // Update the `clicked` field in the Firebase database
        database.child("clicked").setValue(true)
            .addOnSuccessListener {
                Log.d(TAG, "Notification click logged successfully for ID: $notificationId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to log notification click for ID: $notificationId: ${e.message}")
            }
    }
}
