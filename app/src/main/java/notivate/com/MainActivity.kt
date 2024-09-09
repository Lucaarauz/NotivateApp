package notivate.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var screenTimeReceiver: BroadcastReceiver
    private var screenOnStartTime: Long = 0L // Time when screen was turned on
    private var totalScreenTime: Long = 0L // Accumulated screen time in milliseconds
    private val oneHourInMillis: Long = 60 * 60 * 1000 // 1 hour in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize and register the BroadcastReceiver for screen on/off events
        registerScreenTimeReceiver()

        // Set up the button for manual notification (optional for testing purposes)
        val notifyButton: Button = findViewById(R.id.notifyButton)
        notifyButton.setOnClickListener {
            // Manually trigger a notification (e.g., for testing)
            triggerNotification()
        }
    }

    // Function to register BroadcastReceiver to track screen on/off events
    private fun registerScreenTimeReceiver() {
        screenTimeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        // Screen turned on, start counting screen-on time
                        screenOnStartTime = SystemClock.elapsedRealtime()
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        // Screen turned off, accumulate the screen-on time
                        if (screenOnStartTime != 0L) {
                            totalScreenTime += SystemClock.elapsedRealtime() - screenOnStartTime
                            screenOnStartTime = 0L
                        }

                        // Check if total screen time has exceeded 1 hour
                        if (totalScreenTime >= oneHourInMillis) {
                            // Trigger notification to remind the user to take a break
                            triggerNotification()
                            totalScreenTime = 0L // Reset the screen-on time after notification
                        }
                    }
                }
            }
        }

        // Register receiver for screen on/off events
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenTimeReceiver, filter)
    }

    // Function to trigger the notification
    private fun triggerNotification() {
        val notificationIntent = Intent(this, NotificationReceiver::class.java)
        sendBroadcast(notificationIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the screen time receiver to avoid memory leaks
        unregisterReceiver(screenTimeReceiver)
    }
}