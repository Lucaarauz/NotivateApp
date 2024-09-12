package notivate.com

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class NotificationClickActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log the click data to Firebase
        sendClickDataToFirebase()

        // Close the activity once the data is sent (if it's just logging the click)
        finish()
    }

    private fun sendClickDataToFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("clicks")
        val clickData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "notificationId" to "unique_notification_id"  // Add more data if needed
        )

        database.push().setValue(clickData)
            .addOnSuccessListener {
                Log.d("Firebase", "Click data sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to send data: ${e.message}")
            }
    }
}