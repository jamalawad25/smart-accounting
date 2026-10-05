package com.smart.accounting.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val arLocale = Locale("ar")
    private val dayFmt = SimpleDateFormat("EEEE", arLocale)
    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    fun formatArabic(cal: Calendar): String {
        val d = cal.time
        return "${dayFmt.format(d)}، ${dateFmt.format(d)}"
    }

    fun formatArabic(ts: Long): String =
        formatArabic(Calendar.getInstance().apply { timeInMillis = ts })
}