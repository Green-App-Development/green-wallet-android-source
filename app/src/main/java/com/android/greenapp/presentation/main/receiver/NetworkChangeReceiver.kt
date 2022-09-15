package com.android.greenapp.presentation.main.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.greenapp.presentation.custom.isOnline
import com.example.common.tools.VLog

class NetworkChangeReceiver : BroadcastReceiver() {


    override fun onReceive(p0: Context, p1: Intent) {
        val res = isOnline(p0)
        VLog.d("result of if online or not : $res")
        if (isOnline(p0)) {

        }
    }




}