package notivate.com

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {
    private val CHANNELID = "notification_channel"
    private val NOTIFICATIONID = 1
    private val PERMISSION_REQUEST_CODE = 100
    private val EXACT_ALARM_REQUEST_CODE = 200
    private val notificationDelay = 10000 // in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create notification channel if necessary
        createNotificationChannel()

        val notifyButton: Button = findViewById(R.id.notifyButton)
        notifyButton.setOnClickListener {
            checkAndSendNotification()
        }
    }

    private fun createNotificationChannel() {
        val name = "Notification Channel"
        val descriptionText = "Channel for sending notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNELID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun checkAndSendNotification() {
        if (isNotificationPermissionGranted()) {
            if (canScheduleExactAlarms()) {
                scheduleNotificationWithAlarm()
            } else {
                requestExactAlarmPermission()
            }
        } else {
            requestNotificationPermission()
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun canScheduleExactAlarms(): Boolean {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    private fun requestExactAlarmPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        startActivityForResult(intent, EXACT_ALARM_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (canScheduleExactAlarms()) {
                    scheduleNotificationWithAlarm()
                } else {
                    requestExactAlarmPermission()
                }
            } else {
                // Permission denied, handle this case (e.g., show a message, disable functionality)
            }
        }
    }

    private fun scheduleNotificationWithAlarm() {
        val intent = Intent(this, NotificationService::class.java).apply {
            putExtra("channelId", CHANNELID)
            putExtra("notificationId", NOTIFICATIONID)
        }
        val pendingIntent = PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + notificationDelay,
            pendingIntent
        )
    }
}