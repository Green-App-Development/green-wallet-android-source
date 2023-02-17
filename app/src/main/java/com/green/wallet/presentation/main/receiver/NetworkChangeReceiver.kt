package com.green.wallet.presentation.main.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.green.wallet.presentation.tools.VLog

class NetworkChangeReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent) {
        VLog.d("Intent got called : ${intent.action}")
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val networkInfo: NetworkInfo? =
                intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO)
            if (networkInfo != null && networkInfo.detailedState === NetworkInfo.DetailedState.CONNECTED) {
                VLog.d("Network", "Internet YAY")
            } else if (networkInfo != null && networkInfo.detailedState === NetworkInfo.DetailedState.DISCONNECTED) {
                VLog.d("Network", "No internet :(")
            }
        }
    }


}
