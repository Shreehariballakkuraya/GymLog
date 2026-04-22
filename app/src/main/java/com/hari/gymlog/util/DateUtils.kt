package com.hari.gymlog.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    // Standard ISO 8601 date format for database: yyyy-MM-dd
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Display date format: e.g. "Today, Oct 24"
    private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun getCurrentDateForDb(): String {
        return dbDateFormat.format(Date())
    }

    fun getTodayDisplayDate(): String {
        return "Today, " + displayDateFormat.format(Date())
    }
}
