package com.android.greenapp.presentation.custom

import android.app.Activity
import android.app.Dialog
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.android.greenapp.R
import com.android.greenapp.presentation.di.application.AppScope
import com.example.common.tools.VLog
import com.android.greenapp.presentation.tools.getStringResource
import javax.inject.Inject

/**
 * Created by bekjan on 24.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */


@AppScope
class DialogManager @Inject constructor(private val newBtnEffectInstance: AnimationManager) {

    private var progressDialog: Dialog? = null
    private var successDialog: Dialog? = null
    private var failureDialog: Dialog? = null

    fun showSuccessDialog(
        activity: Activity,
        status: String,
        description: String,
        action: String,
        callback: () -> Unit
    ) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_send_money_success)
        val width = activity.resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.apply {
            findViewById<TextView>(R.id.txtStatus).text = status
            findViewById<TextView>(R.id.txtDescription).text = description
            val btnAction = findViewById<Button>(R.id.btnAction)
            btnAction.text = action
            btnAction.setOnClickListener {
                callback()
                dialog.dismiss()
            }
        }
        successDialog?.dismiss()
        successDialog = dialog
        dialog.show()
    }

    fun showFailureDialog(
        activity: Activity,
        status: String,
        description: String,
        action: String,
        callback: () -> Unit
    ) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_send_money_failed)
        val width = activity.resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.apply {
            findViewById<TextView>(R.id.txtStatus).text = status
            findViewById<TextView>(R.id.txtDescription).text = description
            val btnAction = findViewById<Button>(R.id.btnAction)
            btnAction.text = action
            btnAction.setOnClickListener {
                dialog.dismiss()
                callback()
            }
        }
        failureDialog?.dismiss()
        failureDialog = dialog
        dialog.show()
    }


    fun showConfirmationDialog(
        activity: Activity,
        status: String,
        statement: String,
        callbackConfirm: () -> Unit,
        callBackCancel: () -> Unit
    ) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_confirm_delete_wallet)
        val width = activity.resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<TextView>(R.id.txt_confirm_title).text = status
        dialog.findViewById<TextView>(R.id.txtConfirmDialogPhrase).text = statement

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnCancel.setOnClickListener {
            dialog.dismiss()
            callBackCancel()
        }

        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            callbackConfirm()
        }

        dialog.show()
    }


    fun showProgress(activity: Activity): Dialog {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_loading)
        val width = activity.resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.apply {
//            findViewById<TextView>(R.id.txtLoading).text = status
//            findViewById<TextView>(R.id.txtDescription).text = description
        }
        progressDialog?.dismiss()
        progressDialog = dialog
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        return dialog
    }

    fun showCustomProgress(activity: Activity, title: String, description: String): Dialog {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_loading)
        val width = activity.resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.apply {
            findViewById<TextView>(R.id.txtLoading).text = title
            findViewById<TextView>(R.id.txtDescription).text = description
        }
        progressDialog?.dismiss()
        progressDialog = dialog
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        return dialog
    }


    fun hideProgress() {
        if (progressDialog == null) {
            VLog.d("ProgressDialog  is null there nothing to dismiss")
        } else {
            VLog.d("ProgressDialog is not null it is hidden")
            progressDialog!!.dismiss()
        }
    }


    fun showServerErrorDialog(activity: Activity, callback: () -> Unit) {
        activity.apply {
            showFailureDialog(
                this,
                getStringResource(R.string.pop_up_failed_create_a_mnemonic_phrase_title),
                getStringResource(R.string.pop_up_failed_error_description),
                getStringResource(R.string.return_btn)
            ) {
                callback()
            }
        }
    }

    fun showBlockChainIsNotAvailableDialog(activity: Activity, callback: () -> Unit) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_send_money_failed)
        val width = activity.resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.apply {
            findViewById<TextView>(R.id.txtStatus).text =
                activity.getStringResource(R.string.pop_up_failed_error_title)
            findViewById<TextView>(R.id.txtDescription).text =
                activity.getStringResource(R.string.pop_up_failed_error_description_blockchain_not_available)
            val btnAction = findViewById<Button>(R.id.btnAction)
            btnAction.text = activity.getStringResource(R.string.return_btn)
            btnAction.setOnClickListener {
                dialog.dismiss()
                callback()
            }
        }
        failureDialog?.dismiss()
        failureDialog = dialog
        dialog.show()
    }


}