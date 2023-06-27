package com.example.common.tools

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.room.Index.Order
import com.green.wallet.R
import com.green.wallet.presentation.tools.OrderStatus
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getStringResource
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
fun formattedTimeForOrderItem(value: Long) =
	SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(value))

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
		"Chives TestNet" -> "txcc"
		else -> "xch"
	}
	return prefix
}

fun getTokenPrecisionByCode(code: String): Double {
	return when (code) {
		"XCC" -> Math.pow(
			10.0,
			8.0
		)

		"XCH" ->
			Math.pow(
				10.0,
				12.0
			)

		else -> 1000.0
	}
}

fun getTokenPrecisionAfterComoByTokenCode(code: String): Int {
	return when (code) {
		"XCC" -> 8
		"XCH" -> 12
		else -> 3
	}
}

fun requestDateFormat(timeCreated: Long): String {
	return SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(timeCreated))
}

fun convertStringToRequestStatus(status: String): OrderStatus {
	OrderStatus.values().forEach {
		if (it == OrderStatus.valueOf(status))
			return it
	}
	return OrderStatus.InProgress
}

fun getRequestStatusTranslation(activity: Activity, status: OrderStatus): String {
	activity.apply {
		return when (status) {
			OrderStatus.InProgress -> getStringResource(R.string.status_in_process)
			OrderStatus.Waiting -> getStringResource(R.string.awaiting_payment)
			OrderStatus.Success -> getStringResource(R.string.status_completed)
			else -> getStringResource(R.string.status_canceled)
		}
	}
}

fun getRequestStatusColor(status: OrderStatus, activity: Activity): Int {
	return activity.getColorResource(
		when (status) {
			OrderStatus.InProgress -> R.color.orange
			OrderStatus.Waiting -> R.color.blue_aspect_ratio
			OrderStatus.Success -> R.color.green
			else -> R.color.red_mnemonic
		}
	)
}

fun convertArrayStringToList(str: String): List<String> {
	val withoutBrakes = str.substring(1, str.length - 1)
	return withoutBrakes.split(",").map { it.trim() }.toList()
}

fun formatString(begin: Int, str: String, end: Int): String {
	try {
		val ans = StringBuilder()
		ans.append(str.substring(0, begin)).append("...")
		ans.append(str.substring(str.length - end))
		return ans.toString()
	} catch (ex: Exception) {
		return str
	}
}

fun mapNetworkOrderStatusToLocal(status: String): OrderStatus {
	return when (status) {
		"wait" -> OrderStatus.Waiting
		"process" -> OrderStatus.InProgress
		"success" -> OrderStatus.Success
		else -> OrderStatus.Cancelled
	}
}

fun convertNetworkTypeForFlutter(networkType: String): String {
	val lowercaseNetwork = networkType.lowercase()
	if (lowercaseNetwork.contains("chia")) {
		if (lowercaseNetwork.contains("testnet"))
			return "Chia TestNet"
		return "Chia"
	}
	if (lowercaseNetwork.contains("testnet"))
		return "Chives TestNet"
	return "Chives"
}




