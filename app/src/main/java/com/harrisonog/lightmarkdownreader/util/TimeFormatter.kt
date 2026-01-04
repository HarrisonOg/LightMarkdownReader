package com.harrisonog.lightmarkdownreader.util

import android.content.Context
import com.harrisonog.lightmarkdownreader.R
import kotlin.math.abs

object TimeFormatter {

    /**
     * Format a timestamp into a human-readable relative time string
     * Examples: "Just now", "5 minutes ago", "2 hours ago", "Yesterday", "3 days ago"
     */
    fun formatRelativeTime(context: Context, timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp

        // Handle future timestamps (shouldn't happen, but just in case)
        if (diffMillis < 0) {
            return context.getString(R.string.time_just_now)
        }

        val diffMinutes = diffMillis / (60 * 1000)
        val diffHours = diffMillis / (60 * 60 * 1000)
        val diffDays = diffMillis / (24 * 60 * 60 * 1000)

        return when {
            diffMinutes < 1 -> context.getString(R.string.time_just_now)
            diffMinutes < 60 -> context.getString(R.string.time_minutes_ago, diffMinutes.toInt())
            diffHours < 24 -> context.getString(R.string.time_hours_ago, diffHours.toInt())
            diffDays == 1L -> context.getString(R.string.time_yesterday)
            diffDays < 7 -> context.getString(R.string.time_days_ago, diffDays.toInt())
            else -> {
                // For files older than a week, show days ago
                context.getString(R.string.time_days_ago, diffDays.toInt())
            }
        }
    }
}
