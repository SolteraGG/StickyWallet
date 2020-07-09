package stickyWallet.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object TimeUtils {

    private const val DATE_FORMAT_NOW = "dd.MM.yyyy HH:mm:ss"
    private const val DATE_FORMAT_DAY = "dd.MM.yyyy"

    fun now(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
        return sdf.format(cal.time)
    }

    fun `when`(time: Long): String {
        val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
        return sdf.format(time)
    }

    fun date(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(DATE_FORMAT_DAY)
        return sdf.format(cal.time)
    }

    fun clock(seconds: Boolean): String {
        val zone = TimeZone.getTimeZone("Europe/London")
        val date = Date()

        val sdf = SimpleDateFormat(if (seconds) {
            "HH:mm:ss"
        } else {
            "HH:mm"
        })
        sdf.timeZone = zone
        return sdf.format(date)
    }
}
