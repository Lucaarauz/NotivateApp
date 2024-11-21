package notivate.com

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var screenTimeReceiver: ScreenTimeReceiver

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

        // Check usage access permission and show screentime
        if (isUsageAccessGranted()) {
            showDailyScreenTime()
        } else {
            showPermissionDeniedMessage("Screentime permission denied. Please enable it in settings.")
        }

        // Register screen on/off event receiver
        registerScreenTimeReceiver()

        // Trigger notification service if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            triggerNotificationService()
        } else {
            showPermissionDeniedMessage()
        }
    }

    // Request notification permission for Android 13 (API 33+) and above
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Check if usage access permission is granted
    private fun isUsageAccessGranted(): Boolean {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val applicationInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            applicationInfo.uid,
            applicationInfo.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Show permission denied message with instructions
    private fun showPermissionDeniedMessage(message: String? = null) {
        val finalMessage = message ?: "Notification permission is required to send reminders. Please enable it in the app settings."
        Snackbar.make(
            findViewById(android.R.id.content),
            finalMessage,
            Snackbar.LENGTH_LONG
        ).setAction("Settings") {
            openAppSettings()
        }.show()
    }

    // Open app settings if the user denies permission
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun showDailyScreenTime() {
        val screentimeTextView: TextView = findViewById(R.id.screentime_display)
        val screenTime = getDailyScreenTime()
        screentimeTextView.text = "Today's Screen Time: $screenTime"
    }

    private fun getDailyScreenTime(): String {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        var totalForegroundTime = 0L
        if (stats != null) {
            for (stat in stats) {
                totalForegroundTime += stat.totalTimeInForeground
            }
        }

        return formatTime(totalForegroundTime)
    }

    private fun formatTime(milliseconds: Long): String {
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format("%dh, %dm", hours, minutes)
        } else {
            String.format("%dm", minutes)
        }
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

    // Start the NotificationService to send a notification
    private fun triggerNotificationService() {
        val notificationIntent = Intent(this, NotificationService::class.java).apply {
            putExtra("send_now", true)  // Indicate that we want to send a notification now
        }
        startService(notificationIntent)
    }

    // Unregister the BroadcastReceiver when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenTimeReceiver)
    }
}
