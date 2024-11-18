package notivate.com

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class NotificationClickActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the key from the intent
        val notificationKey = intent.getStringExtra("notification_key")

        if (notificationKey != null) {
            Log.d("NotificationClickActivity", "Notification clicked. Key: $notificationKey")
            updateNotificationClickedInFirebase(notificationKey)
        } else {
            Log.e("NotificationClickActivity", "No notification key found in the intent.")
        }

        // Close this activity after updating
        finish()
    }

    private fun updateNotificationClickedInFirebase(notificationKey: String) {
        val database = FirebaseDatabase.getInstance().getReference("notifications")
        val updateData = mapOf("clicked" to true)

        // Update the clicked field of the notification
        database.child(notificationKey).updateChildren(updateData)
            .addOnSuccessListener {
                Log.d("Firebase", "Notification click updated successfully for key: $notificationKey")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to update notification click: ${e.message}")
            }
    }
}