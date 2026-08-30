package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    val fullDateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthShortFormatter = SimpleDateFormat("MMM", Locale.getDefault())
    val dayOfWeekFormatter = SimpleDateFormat("EEE", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayOfMonthFormatter = SimpleDateFormat("d", Locale.getDefault())

    private val iso8601UtcFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }
    private val iso8601NoMillisFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }

    fun formatIso8601(epochMillis: Long): String {
        return iso8601UtcFormatter.format(Date(epochMillis))
    }

    fun parseIso8601(isoString: String?): Long? {
        if (isoString.isNullOrBlank()) return null
        val clean = isoString.replace("+00:00", "Z").replace("+00", "Z")
        return try {
            iso8601UtcFormatter.parse(clean)?.time
                ?: iso8601NoMillisFormatter.parse(clean)?.time
        } catch (e: Exception) {
            try {
                iso8601NoMillisFormatter.parse(clean)?.time
            } catch (e2: Exception) {
                try {
                    isoDateFormatter.parse(clean)?.time
                } catch (e3: Exception) {
                    null
                }
            }
        }
    }

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isSameDay(epoch1: Long, epoch2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = epoch1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = epoch2 }
        return isSameDay(c1, c2)
    }

    fun isToday(epoch: Long): Boolean {
        return isSameDay(epoch, System.currentTimeMillis())
    }

    fun formatEventTimeRange(startEpoch: Long, endEpoch: Long?, allDay: Boolean): String {
        if (allDay) return "All Day"
        val startStr = timeFormatter.format(Date(startEpoch))
        if (endEpoch == null) return startStr
        val endStr = timeFormatter.format(Date(endEpoch))
        return "$startStr – $endStr"
    }

    fun getStartOfDay(calendar: Calendar): Long {
        val c = calendar.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun getEndOfDay(calendar: Calendar): Long {
        val c = calendar.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    fun getDaysInMonthGrid(calendar: Calendar): List<CalendarDay> {
        val c = calendar.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, 1)

        val month = c.get(Calendar.MONTH)
        val year = c.get(Calendar.YEAR)

        // Day of week for 1st of month (Sunday is 1 in Calendar)
        val firstDayOfWeek = c.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        val daysBefore = (firstDayOfWeek - Calendar.SUNDAY)

        val days = mutableListOf<CalendarDay>()

        // Previous month days
        val prevMonthCal = c.clone() as Calendar
        prevMonthCal.add(Calendar.MONTH, -1)
        val maxDaysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in (maxDaysInPrevMonth - daysBefore + 1)..maxDaysInPrevMonth) {
            val dayCal = prevMonthCal.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, i)
            days.add(
                CalendarDay(
                    calendar = dayCal,
                    dayNumber = i,
                    isCurrentMonth = false,
                    isToday = isSameDay(dayCal, Calendar.getInstance())
                )
            )
        }

        // Current month days
        val maxDaysInCurrent = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDaysInCurrent) {
            val dayCal = c.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, i)
            days.add(
                CalendarDay(
                    calendar = dayCal,
                    dayNumber = i,
                    isCurrentMonth = true,
                    isToday = isSameDay(dayCal, Calendar.getInstance())
                )
            )
        }

        // Next month trailing days to complete grid (up to 35 or 42 cells)
        val remaining = (7 - (days.size % 7)) % 7
        val nextMonthCal = c.clone() as Calendar
        nextMonthCal.add(Calendar.MONTH, 1)
        for (i in 1..remaining) {
            val dayCal = nextMonthCal.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, i)
            days.add(
                CalendarDay(
                    calendar = dayCal,
                    dayNumber = i,
                    isCurrentMonth = false,
                    isToday = isSameDay(dayCal, Calendar.getInstance())
                )
            )
        }

        return days
    }

    fun getDaysOfWeek(calendar: Calendar): List<CalendarDay> {
        val c = calendar.clone() as Calendar
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        c.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - Calendar.SUNDAY))

        val list = mutableListOf<CalendarDay>()
        for (i in 0..6) {
            val dayCal = c.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, i)
            list.add(
                CalendarDay(
                    calendar = dayCal,
                    dayNumber = dayCal.get(Calendar.DAY_OF_MONTH),
                    isCurrentMonth = true,
                    isToday = isSameDay(dayCal, Calendar.getInstance())
                )
            )
        }
        return list
    }

    fun getNextOccurrenceTime(startTimeEpoch: Long, isRecurring: Boolean, recurrenceRule: String?, reminderMinutesBefore: Int = 0): Long {
        val now = System.currentTimeMillis()
        val originalCal = Calendar.getInstance().apply { timeInMillis = startTimeEpoch }
        val triggerOffset = reminderMinutesBefore * 60 * 1000L

        // If not recurring, return original startTime
        if (!isRecurring && recurrenceRule.isNullOrBlank()) {
            return startTimeEpoch
        }

        val rule = recurrenceRule?.uppercase() ?: "YEARLY"
        return when {
            rule.contains("YEAR") || rule.contains("BIRTHDAY") -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.MONTH, originalCal.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, originalCal.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, originalCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, originalCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                // If this year's trigger has already passed, move to next year
                if (cal.timeInMillis - triggerOffset < now - 60000L) {
                    cal.add(Calendar.YEAR, 1)
                }
                cal.timeInMillis
            }
            rule.contains("MONTH") -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, originalCal.get(Calendar.DAY_OF_MONTH).coerceAtMost(getActualMaximum(Calendar.DAY_OF_MONTH)))
                    set(Calendar.HOUR_OF_DAY, originalCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, originalCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (cal.timeInMillis - triggerOffset < now - 60000L) {
                    cal.add(Calendar.MONTH, 1)
                }
                cal.timeInMillis
            }
            rule.contains("WEEK") -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, originalCal.get(Calendar.DAY_OF_WEEK))
                    set(Calendar.HOUR_OF_DAY, originalCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, originalCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (cal.timeInMillis - triggerOffset < now - 60000L) {
                    cal.add(Calendar.WEEK_OF_YEAR, 1)
                }
                cal.timeInMillis
            }
            rule.contains("DAILY") || rule.contains("DAY") -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, originalCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, originalCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (cal.timeInMillis - triggerOffset < now - 60000L) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                cal.timeInMillis
            }
            else -> startTimeEpoch
        }
    }

    fun getBirthdayCountdownText(birthDateStr: String?): String? {
        if (birthDateStr.isNullOrBlank()) return null
        return try {
            val parts = birthDateStr.split("-")
            if (parts.size != 3) return null
            val bMonth = parts[1].toInt() - 1
            val bDay = parts[2].toInt()

            val now = Calendar.getInstance()
            val currentYear = now.get(Calendar.YEAR)

            val bdayThisYear = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, bMonth)
                set(Calendar.DAY_OF_MONTH, bDay)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val todayStart = getStartOfDay(now)
            val diffMillis = bdayThisYear.timeInMillis - todayStart

            if (diffMillis == 0L) {
                "🎉 Happy Birthday! It's today!"
            } else if (diffMillis > 0) {
                val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis)
                if (daysRemaining == 1L) "🎂 Birthday tomorrow!" else "🎂 Birthday in $daysRemaining days"
            } else {
                // Next year's birthday
                bdayThisYear.add(Calendar.YEAR, 1)
                val daysRemaining = TimeUnit.MILLISECONDS.toDays(bdayThisYear.timeInMillis - todayStart)
                "🎂 Birthday in $daysRemaining days"
            }
        } catch (e: Exception) {
            null
        }
    }
}

data class CalendarDay(
    val calendar: Calendar,
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)
