package com.example.common.tools

import timber.log.Timber

object VLog {

    fun d(message: String, tag: String = com.android.greenapp.presentation.tools.GENERAL_TAG) {
        Timber.tag(tag).d(message)
    }

    fun e(message: String,tag:String = com.android.greenapp.presentation.tools.GENERAL_TAG) {
        Timber.tag(tag).e(message)
    }


}