package com.android.greenapp.presentation.custom

import android.app.Activity
import com.android.greenapp.R
import com.android.greenapp.presentation.tools.getStringResource
import java.net.SocketTimeoutException
import java.net.UnknownHostException


fun parseException(code: Int) {
	return when (code) {
		1007 -> throw ServerMaintenanceExceptions()
		else -> throw java.lang.Exception()
	}
}

class ServerMaintenanceExceptions : Exception()

fun manageExceptionDialogsForRest(
	activity: Activity,
	dialogManager: DialogManager,
	exception: Exception?
) {

	when (exception) {
		is SocketTimeoutException -> {
			dialogManager.showNoInternetTimeOutExceptionDialog(activity) {

			}
		}
		is UnknownHostException -> {
			dialogManager.showNoInternetTimeOutExceptionDialog(activity) {

			}
		}
		is ServerMaintenanceExceptions -> {
			activity.apply {
				dialogManager.showFailureDialog(
					this,
					status = getStringResource(R.string.address_book_pop_up_delete_title),
					description = getStringResource(R.string.server_maintenance),
					action = getStringResource(R.string.ok_button)
				) {

				}
			}
		}
		else -> {
			dialogManager.showServerErrorDialog(activity) {

			}
		}
	}
}



