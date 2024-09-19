package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var screenTimeReceiver: BroadcastReceiver

    // Permission request launcher for notifications (API 33+)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showPermissionDeniedMessage()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request notification permission if needed for API 33+
        requestNotificationPermission()

        // Initialize and register the BroadcastReceiver
        registerScreenTimeReceiver()

        // Button for manual notification
        val notifyButton: Button = findViewById(R.id.notifyButton)
        notifyButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                sendNotification()
            } else {
                showPermissionDeniedMessage()
            }
        }
    }

    // Request notification permission for Android 13 (API 33+) and above
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Show a message to the user when the notification permission is denied
    private fun showPermissionDeniedMessage() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Notification permission is required to send reminders. Please enable it in the app settings.",
            Snackbar.LENGTH_LONG
        ).setAction("Settings") {
            openAppSettings()
        }.show()
    }

    // Open app settings if the user denies the permission and clicks on the "Settings" button
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    // Register the BroadcastReceiver for screen on/off events
    private fun registerScreenTimeReceiver() {
        screenTimeReceiver = ScreenTimeReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenTimeReceiver, filter)
    }

    // Send a notification
    private fun sendNotification() {
        val title = "Test Notification"
        val text = "This notification was sent from MainActivity!"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification) // Use your own notification icon here
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    // Unregister the BroadcastReceiver when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenTimeReceiver)
    }

    companion object {
        private const val CHANNEL_ID = "notification_channel"
        private const val NOTIFICATION_ID = 1
    }
}
