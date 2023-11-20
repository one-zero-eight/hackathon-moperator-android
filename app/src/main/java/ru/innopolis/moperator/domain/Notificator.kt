package ru.innopolis.moperator.domain

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.innopolis.moperator.MainActivity
import ru.innopolis.moperator.R
import java.nio.charset.Charset

class Notificator(private val mContext: android.content.Context) {
    private var token: String? = null
    private val queue = com.android.volley.toolbox.Volley.newRequestQueue(mContext)
    private var notificationId = 1
    private val mChannel = NotificationChannel(
        "MOPERATOR_CHANNEL",
        "Moperator",
        NotificationManager.IMPORTANCE_HIGH
    )

    private fun sendNotification(
        notification: org.json.JSONObject,
    ) {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Notification", "No permission to send notifications")
            return
        }

        val title = notification.getString("title")
        val description = notification.getString("description")

        val builder = NotificationCompat.Builder(mContext, "MOPERATOR_CHANNEL")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(mContext)) {
            // notificationId is a unique int for each notification that you must define
            Log.d("Notification", "Sending notification")
            notify(notificationId, builder.build())
            notificationId += 1
        }

    }

    fun checkForUpdates() {
        if (token == null) {
            Log.d("Notification", "Token is null")
            return
        }
        // send request using Volley
        val url = "https://api.moperator.innohassle.ru/users/my-notifications"
        // Add Authorization header to the request
        val request = object : com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.GET, url,
            com.android.volley.Response.Listener { response ->
                Log.d("Notification", "Response is: $response")
                val decodedToUTF8 = String(
                    response.toByteArray(Charset.forName("ISO-8859-1")),
                    Charset.forName("UTF-8")
                )
                val notifications = org.json.JSONArray(decodedToUTF8)
                for (i in 0 until notifications.length()) {
                    val notification = notifications.getJSONObject(i)
                    sendNotification(notification)
                }
            },
            com.android.volley.Response.ErrorListener { error ->
                Log.d("Notification", "That didn't work!")
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = java.util.HashMap()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }
        queue.add(request)
    }

    init {
        val notificationManager = mContext.getSystemService(
            android.content.Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)


        val looper = android.os.Looper.getMainLooper()
        val handler = Handler(looper)
        val delay = 5000.toLong() // 1000 milliseconds == 1 second


        handler.postDelayed(object : Runnable {
            override fun run() {
                Log.d("Notification", "Update...") // Do your work here
                checkForUpdates()

                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    fun setToken(token: String) {
        Log.d("Notification", "Token is set to $token")
        this.token = token
    }
}