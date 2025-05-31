package com.example.pocketdeutsch.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun formatDateTime(date: Date): String {
        val dateFormat = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun parseDate(dateString: String): Date? {
        return try {
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun parseDateTime(dateTimeString: String): Date? {
        return try {
            val dateFormat = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())
            dateFormat.parse(dateTimeString)
        } catch (e: Exception) {
            null
        }
    }

    fun isToday(dateString: String): Boolean {
        return dateString == getCurrentDate()
    }

    fun getDaysDifference(fromDate: String, toDate: String): Long {
        val fromDateObj = parseDate(fromDate) ?: return 0
        val toDateObj = parseDate(toDate) ?: return 0

        val diffInMillis = toDateObj.time - fromDateObj.time
        return diffInMillis / (1000 * 60 * 60 * 24)
    }

    fun addDays(dateString: String, days: Int): String {
        val date = parseDate(dateString) ?: return dateString
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return formatDate(calendar.time)
    }
}