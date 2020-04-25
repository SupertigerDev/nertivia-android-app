package com.supertiger.nertivia

import java.util.*


fun friendlyDate(created: Long?): String {
    if (created === null) {
        return "none"
    }
    val cal = Calendar.getInstance()
    cal.timeInMillis = created

    if (isSameDay(cal)) {
        return "Today at ${getFullTime(cal)}"
    } else if (isYesterday(cal)) {
        return "Yesterday at ${getFullTime(cal)}"
    } else {
        return getFullDateWithTime(cal)
    }
}

private fun isSameDay(cal: Calendar): Boolean {
    val now = Calendar.getInstance()
    return (
        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)&&
        cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)&&
        cal.get(Calendar.DATE) == now.get(Calendar.DATE)
    )
}

private fun isYesterday(cal: Calendar): Boolean {
    val now = Calendar.getInstance()
    return (
        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)&&
        cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)&&
        now.get(Calendar.DATE) - cal.get(Calendar.DATE) == 1
    )
}

private fun getFullTime(cal: Calendar): String {
    val hours = cal.get(Calendar.HOUR_OF_DAY)
    val minutes = cal.get(Calendar.MINUTE)
    var finalTime: String

    if (hours <= 9) {
        finalTime = "0$hours"
    } else {
        finalTime = "$hours"
    }

    if (minutes <= 9) {
        finalTime += ":0$minutes"
    } else {
        finalTime += ":$minutes"
    }

    return finalTime
}


private fun getFullDateWithTime(cal: Calendar): String {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    val dayName = days[cal.get(Calendar.DAY_OF_WEEK) - 1];
    val monthName = months[cal.get(Calendar.MONTH)]
    return "$dayName ${cal.get(Calendar.DAY_OF_MONTH)} $monthName ${cal.get(Calendar.YEAR)} at ${getFullTime(cal)}"

}

//val day = cal.get(Calendar.DATE)
//val month = cal.get(Calendar.MONTH)
//val year = cal.get(Calendar.YEAR);