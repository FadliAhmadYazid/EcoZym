package com.ecozym.wastemanagement.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    fun formatDate(timestamp: Timestamp): String {
        return dateFormat.format(timestamp.toDate())
    }

    fun formatTime(timestamp: Timestamp): String {
        return timeFormat.format(timestamp.toDate())
    }

    fun formatDateTime(timestamp: Timestamp): String {
        return dateTimeFormat.format(timestamp.toDate())
    }

    fun getTimeAgo(timestamp: Timestamp): String {
        val now = System.currentTimeMillis()
        val time = timestamp.toDate().time
        val diff = now - time

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            diff < 604800_000 -> "${diff / 86400_000}d ago"
            else -> formatDate(timestamp)
        }
    }
}