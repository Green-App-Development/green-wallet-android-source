package com.android.greenapp.presentation.custom

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.android.greenapp.presentation.custom.dialogs.NetworkAdapter
import com.android.greenapp.presentation.tools.getColorResource
import com.android.greenapp.presentation.tools.getStringResource
import com.example.common.tools.VLog
import com.example.common.tools.formattedDateMonth
import com.example.common.tools.formattedHourMinute
import com.example.common.tools.getCurrentTimeInMinutes
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.DecimalFormat


fun showBtmDialogChoosingNetworks(
    hasAtLeastOneWallet: Boolean,
    activity: Activity,
    networkList: List<String>,
    chooseCallBack: (Int) -> Unit
) {
    val dialog = BottomSheetDialog(activity, R.style.AppBottomSheetDialogTheme)
    val sheetView = activity.layoutInflater.inflate(R.layout.dialog_choose_network, null)
    dialog.setContentView(sheetView)
    val networkRecView = dialog.findViewById<RecyclerView>(R.id.rec_view_network)
    val networkAdapter = NetworkAdapter(activity, object : NetworkAdapter.ChooseNetworkListener {
        override fun onNetworkClicked(position: Int) {
            chooseCallBack(position)
            dialog.dismiss()
        }
    })
    networkRecView?.adapter = networkAdapter
    networkAdapter.updateNetworkList(networkList)
    dialog.findViewById<TextView>(R.id.txtSelectNetwork)?.setText(
        activity.getStringResource(if (hasAtLeastOneWallet) R.string.select_network else R.string.select_network_new_wallet)
    )
    dialog.show()
}


fun showBtmAddOrImportWalletDialog(
    activity: Activity,
    listener: ChooseWalletImportOrNewListener
): Dialog {
    val dialog = BottomSheetDialog(activity, R.style.AppBottomSheetDialogTheme)
    val sheetView = activity.layoutInflater.inflate(R.layout.dialog_add_wallet, null)
    dialog.setContentView(sheetView)
    dialog.findViewById<RelativeLayout>(R.id.rel_new_wallet)?.setOnClickListener {
        listener.newClicked(it)
        dialog.dismiss()
    }
    dialog.findViewById<RelativeLayout>(R.id.rel_import)?.setOnClickListener {
        listener.importClicked(it)
        dialog.dismiss()
    }
    dialog.show()
    return dialog
}

interface ChooseWalletImportOrNewListener {
    fun newClicked(v: View)
    fun importClicked(v: View)
}


fun getStatusBarBcgColorBasedOnTime(): Boolean {
    val curMinutes = getCurrentTimeInMinutes()
    val isLightBar = when {
        curMinutes in 0..com.android.greenapp.presentation.tools.MORNING_STARTS_MINUTE -> false
        curMinutes in com.android.greenapp.presentation.tools.MORNING_STARTS_MINUTE..com.android.greenapp.presentation.tools.NOON_STARTS_MINUTE -> false
        curMinutes in com.android.greenapp.presentation.tools.NOON_STARTS_MINUTE..com.android.greenapp.presentation.tools.EVENING_STARTS_MINUTE -> true
        else -> false
    }
    return isLightBar
}

fun getStatusBcgBarColorBasedOnTime(activity: Activity): Int {
    val curMinutes = getCurrentTimeInMinutes()
    val color = when {
        curMinutes in 0..com.android.greenapp.presentation.tools.MORNING_STARTS_MINUTE -> activity.getColorResource(
            R.color.night_status_bar_clr
        )
        curMinutes in com.android.greenapp.presentation.tools.MORNING_STARTS_MINUTE..com.android.greenapp.presentation.tools.NOON_STARTS_MINUTE -> activity.getColorResource(
            R.color.morning_status_bar_clr
        )
        curMinutes in com.android.greenapp.presentation.tools.NOON_STARTS_MINUTE..com.android.greenapp.presentation.tools.EVENING_STARTS_MINUTE -> activity.getColorResource(
            R.color.day_status_bar_clr
        )
        else -> activity.getColorResource(R.color.evening_status_bar_clr)
    }
    return color
}

fun getTranslatedMonth(activity: Activity, num: Int): String {
    activity.apply {
        val map = mapOf(
            1 to getStringResource(R.string.month_january),
            2 to getStringResource(R.string.month_february),
            3 to getStringResource(R.string.month_march),
            4 to getStringResource(R.string.month_april),
            5 to getStringResource(R.string.month_may),
            6 to getStringResource(R.string.month_june),
            7 to getStringResource(R.string.month_july),
            8 to getStringResource(R.string.month_august),
            9 to getStringResource(R.string.month_september),
            10 to getStringResource(R.string.month_october),
            11 to getStringResource(R.string.month_november),
            12 to getStringResource(R.string.month_december)
        )
        return map[num] ?: ""
    }
}

fun hidePublicKey(fingerPrint: Long): String {
    val publicKey = fingerPrint.toString()
    if (publicKey.length <= 4) return publicKey
    return "*".repeat(publicKey.length - 4) + publicKey.substring(publicKey.length - 4)
}


fun formattedDateForTransaction(activity: Activity, time: Long): String {
    //pattern 23 January, 12:10
    val dayMonth = formattedDateMonth(time).split(":")
    VLog.d("Formatted DayMonth : $dayMonth")
    return "${dayMonth[0]} ${
        getTranslatedMonth(
            activity,
            dayMonth[1].toInt()
        )
    }, ${formattedHourMinute(time)}"
}

fun getShortNetworkType(networkType: String): String {
    return if (networkType.lowercase().contains("chia")) "XCH" else "XCC"
}

fun isThisChivesNetwork(networkType: String): Boolean {
    return networkType.lowercase().contains("chives")
}

fun dpFromPx(context: Context, px: Float): Float {
    return px / context.getResources().getDisplayMetrics().density
}


fun formattedDoubleAmountWithPrecision(double: Double): String {
    if (double == 0.0) return "0.0"
    val df = DecimalFormat("0")
    df.maximumFractionDigits = 340
    val formatted = df.format(double).replace(",", ".")
    val dotPos = formatted.indexOf('.')
    val endMin = Math.min(dotPos + 12, formatted.length)
    return formatted.substring(0, endMin)
}

fun formattedDollarWithPrecision(price: Double,precision:Int=2): String {
    val formattedBalance = String.format("%.${precision}f", price).replace(",", ".")
    return formattedBalance
}


fun getPreferenceKeyForBalance(fingerPrint: Long): Preferences.Key<Double> {
    return doublePreferencesKey("${fingerPrint}_balance")
}

fun getPreferenceKeyForCurStockNetworkDouble(type: String): Preferences.Key<Double> {
    return doublePreferencesKey("${type}_network_stock")
}

fun getPreferenceKeyForCurNetworkPrev24ChangeDouble(type: String): Preferences.Key<Double> {
    return doublePreferencesKey("${type}_network_24_hours")
}

fun getPreferenceKeyForNetworkItem(type: String): Preferences.Key<String> {
    return stringPreferencesKey("${type}_network_item")
}

fun getPreferenceKeyForCoinDetail(type: String): Preferences.Key<String> {
    return stringPreferencesKey("${type}_coin_detail")
}

fun trimNetwork(networkType: String): String {
    return networkType.replace("Network", "")
}

fun mnemonicsToString(mnemonics: List<String>): String {
    var str = ""
    for (i in 0 until mnemonics.size) {
        str += mnemonics[i]
        if (i != mnemonics.size - 1)
            str += " "
    }
    return str
}


fun isExceptionBelongsToNoInternet(ex: Exception): Boolean {
    return (ex is SocketTimeoutException) or (ex is UnknownHostException)
}

fun isOnline(context: Context): Boolean {
    val cm: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo: NetworkInfo? = cm.activeNetworkInfo
    //should check null because in airplane mode it will be null
    return netInfo != null && netInfo.isConnected
}





