package notivate.com

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class NotificationClickActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log the click event to Firebase
        logNotificationClickToFirebase()

        // Finish immediately as you don't need any UI
        finish()
    }

    private fun logNotificationClickToFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("notification_clicks")

        // Log a click event to Firebase with a timestamp
        val clickData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "clicked" to true
        )

        database.push().setValue(clickData)
            .addOnSuccessListener {
                Log.d("NotificationClick", "Notification click logged successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationClick", "Failed to log click event: ${e.message}")
            }
    }
}