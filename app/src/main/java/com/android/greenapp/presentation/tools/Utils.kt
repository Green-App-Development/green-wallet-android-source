package com.example.common.tools

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Matcher
import java.util.regex.Pattern


fun setLocal(context: Context, lang: String) {
    val locale = Locale(lang)
    Locale.setDefault(locale)
    val res = context.resources
    val config = res.configuration
    config.setLocale(locale)
    res.updateConfiguration(config, res.displayMetrics)
}

fun Dialog.setDefaultParams(dialogAnim: Int) {
    window?.apply {
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        attributes.windowAnimations = dialogAnim
        setGravity(Gravity.BOTTOM)
    }
}

fun formattedTime(value: Long) = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date(value))

fun formattedDay(value: Long) = SimpleDateFormat("dd.MM.yyyy").format(Date(value))

fun formattedHourMinute(value: Long) = SimpleDateFormat("HH:mm").format(value)

fun randomGenerateTimeWithinLastYear() = ThreadLocalRandom.current()
    .nextLong(System.currentTimeMillis() - (60_000) * 60 * 24 * 5, System.currentTimeMillis())

fun formattedDateMonth(value: Long) = SimpleDateFormat("dd:MM").format(value)

fun formattedTodaysDate() = SimpleDateFormat("dd").format(System.currentTimeMillis())


fun formattedYesterdaysDate() =
    SimpleDateFormat("dd").format(System.currentTimeMillis() - (60_000) * 60 * 24)

fun milliSecondSinceMidNight(value: Long): Long {
    val hourMinute = formattedHourMinute(value).split(":").map { Integer.valueOf(it) }.toList()
    VLog.d("HourMinute for Sorting : $hourMinute")
    return ((hourMinute[1] * (60_000L)) + (60_000 * 60 * hourMinute[0]))
}

fun calculateMillisecondsPassedSinceMidNight(value: Long): Long {
    val milliPassed = milliSecondSinceMidNight(value)
    return System.currentTimeMillis() - milliPassed
}

fun calculateMillisecondsPassedSinceYesterday(value: Long): Long {
    val todayDifference = milliSecondSinceMidNight(value)
    return System.currentTimeMillis() - (todayDifference + (60_000 * 1440L))
}

fun calculateMillisecondsPassedSinceLastWeek(value: Long): Long {
    val todayDifference = milliSecondSinceMidNight(value)
    return System.currentTimeMillis() - (todayDifference + (60_000) * 1440L * 7)
}

fun calculateMillisecondsPassedSinceLastMonth(value: Long): Long {
    val todayDifference = milliSecondSinceMidNight(value)
    return System.currentTimeMillis() - (todayDifference + (60_000) * 1440L * 7 * 4)
}

fun calculateMillisecondsPassedSinceLastYear(value: Long): Long {
    val lastMonthDifference = calculateMillisecondsPassedSinceLastMonth(value)
    return System.currentTimeMillis() - lastMonthDifference * 12
}

fun getCurrentTimeInMinutes(): Int {
    val dateFormat = SimpleDateFormat("HH:mm")
    val time = dateFormat.format(Date(System.currentTimeMillis()))
    val split = time.split(":")
    val minute = Integer.valueOf(split[1])
    return Integer.valueOf(split[0]) * 60 + minute
}

fun getCurrentHourIn24(): Int {
    val format = SimpleDateFormat("HH")
    return Integer.valueOf(format.format(System.currentTimeMillis()))
}

fun convertDateFormatToMilliSeconds(patternDate: String): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = dateFormat.parse(patternDate)
    val epoch = date.time
    return epoch
}

val VALID_EMAIL_ADDRESS_REGEX: Pattern =
    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)

fun validateEmail(emailStr: String?): Boolean {
    val matcher: Matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr)
    return matcher.find()
}

@SuppressLint("SetTextI18n")
fun addingDoubleDotsTxt(txt: TextView) {
    txt.apply {
        txt.text = "${txt.text}:"
    }
}

fun getStartingIndexWordCountToHighlightEndingIndex(langCode: String): IntArray {
    VLog.d("LangCode to HighLightWordsToGreen : $langCode")
    val code = langCode.substring(0, 2)
    if (code == "ru")
        return intArrayOf(6, 14)
    if (code == "de")
        return intArrayOf(4, 26)
    return intArrayOf(2, 27)
}

fun getPrefixForAddressFromNetworkType(networkType: String): String {
    var prefix = when (networkType) {
        "Chia TestNet" -> "txch"
        "Chives Network" -> "xcc"
        "Chia TestNet" -> "txch"
        else  -> "xch"
    }
    return prefix
}







