package com.green.wallet.presentation.custom

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.provider.Settings.ACTION_SETTINGS
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.green.wallet.R
import com.green.wallet.databinding.DialogPriceChangedWarningBinding
import com.green.wallet.databinding.DialogWarningDeleteBinding
import com.green.wallet.databinding.DialogWarningOrderExistBinding
import com.green.wallet.presentation.di.application.AppScope
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getStringResource
import javax.inject.Inject


@AppScope
class DialogManager @Inject constructor(private val newBtnEffectInstance: AnimationManager) {

    private var progressDialog: Dialog? = null
    private var successDialog: Dialog? = null
    private var failureDialog: Dialog? = null
    private var noConnectionDialog: Dialog? = null

    fun showSuccessDialog(
        activity: Activity,
        status: String,
        description: String,
        action: String,
        isDialogOutsideTouchable: Boolean = true,
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
        noConnectionDialog?.dismiss()
        successDialog = dialog
        dialog.setCanceledOnTouchOutside(isDialogOutsideTouchable)
        dialog.show()
    }

    fun showFailureDialog(
        activity: Activity,
        status: String,
        description: String,
        action: String,
        isCancellable: Boolean = true,
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
        dialog.setCanceledOnTouchOutside(isCancellable)
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


    fun hidePrevDialogs() {
        if (progressDialog == null) {
            VLog.d("ProgressDialog  is null there nothing to dismiss")
        } else {
            VLog.d("ProgressDialog is not null it is hidden")
            progressDialog?.dismiss()
        }
        if (noConnectionDialog == null)
            VLog.d("NoConnectionsDialog is null")
        else {
            VLog.d("No Connections Dialog is not null it is hidde")
            noConnectionDialog?.dismiss()
        }
    }

    fun dismissAllPrevDialogs() {
        progressDialog?.dismiss()
        progressDialog = null
        successDialog?.dismiss()
        successDialog = null
        failureDialog?.dismiss()
        failureDialog = null
    }

    fun isProgressDialogShowing() = progressDialog?.isShowing


    fun dismissNoConnectionDialog() {
        if (noConnectionDialog != null && noConnectionDialog!!.isShowing) {
            noConnectionDialog!!.dismiss()
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

    fun showNoInternetTimeOutExceptionDialog(activity: Activity, callback: () -> Unit) {
        if (noConnectionDialog != null && noConnectionDialog!!.isShowing)
            return
        val dialog = Dialog(activity, R.style.DialogTheme)
        val view = activity.layoutInflater.inflate(R.layout.fragment_no_internet, null)
        dialog.setContentView(view)
        view.findViewById<Button>(R.id.btnReConnectBtn).setOnClickListener {
            kotlin.runCatching {
                val intent = Intent(ACTION_SETTINGS)
                activity.startActivity(intent)
            }.onFailure {
                VLog.d("On failure calling network settings menu : $it  ")
            }
        }

        dialog.apply {
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = activity.getColorResource(R.color.primary_app_background)
        }
        noConnectionDialog = dialog
        dialog.show()
    }

    fun showWarningDialogAddress(
        activity: Activity,
        status: String,
        statement: String,
        callbackConfirm: () -> Unit,
        callBackCancel: () -> Unit
    ) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_warning_less_important)
        val width = activity.resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.apply {
            findViewById<TextView>(R.id.txtStatus).text = status
            findViewById<TextView>(R.id.txtDescription).text = statement
            val btnCancel = findViewById<Button>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
                callBackCancel()
            }
            val btnConfirm = findViewById<Button>(R.id.btnConfirm)
            btnConfirm.setOnClickListener {
                dialog.dismiss()
                callbackConfirm()
            }

        }
        failureDialog?.dismiss()
        failureDialog = dialog
        dialog.show()

    }


    fun showAssuranceDialogDefaultSetting(
        activity: Activity,
        status: String,
        statement: String,
        btnYes: () -> Unit,
        btnNo: () -> Unit
    ) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_default_setting)
        val width = activity.resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<TextView>(R.id.txt_confirm_title).text = status
        dialog.findViewById<TextView>(R.id.txtConfirmDialogPhrase).text = statement

        val btnCancel = dialog.findViewById<Button>(R.id.btnNo)

        btnCancel.setOnClickListener {
            dialog.dismiss()
            btnNo()
        }

        val btnConfirm = dialog.findViewById<Button>(R.id.btnYes)

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            btnYes()
        }

        dialog.show()
    }


    fun showQuestionDetailsDialog(
        activity: Activity,
        status: String,
        description: String,
        action: String,
        okay: () -> Unit
    ) {

        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_question_beta)
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
                okay()
                dialog.dismiss()
            }
        }
        successDialog?.dismiss()
        noConnectionDialog?.dismiss()
        successDialog = dialog
        dialog.show()
    }


    fun showQuestionDialogExchange(
        activity: Activity,
        status: String,
        description: String,
        action: String,
        okay: () -> Unit
    ) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_question_exchange)
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
                okay()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    fun showWarningOrderExistDialog(
        activity: Activity,
        status: String,
        statement: String,
        btnText: String,
        cancellable: Boolean = true,
        onClick: () -> Unit
    ) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        val binding = DialogWarningOrderExistBinding.inflate(activity.layoutInflater)
        dialog.setContentView(binding.root)
        val width = activity.resources.displayMetrics.widthPixels
        binding.apply {
            txtStatus.text = status
            txtDescription.text = statement
            btnAction.text = btnText
            btnAction.setOnClickListener {
                dialog.dismiss()
                onClick()
            }
        }
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(cancellable)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


    fun showWarningPriceChangeDialog(
        activity: Activity,
        status: String,
        statement: String,
        btnText: String,
        onClick: () -> Unit
    ) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        val binding = DialogPriceChangedWarningBinding.inflate(activity.layoutInflater)
        dialog.setContentView(binding.root)
        val width = activity.resources.displayMetrics.widthPixels
        binding.apply {
            txtStatus.text = status
            txtDescription.text = statement
            btnAction.text = btnText
            btnAction.setOnClickListener {
                dialog.dismiss()
                onClick()
            }
        }
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun showWarningDeleteTransaction(
        activity: Activity,
        status: String,
        statement: String,
        btnText1: String,
        btnText2: String,
        onCancel: () -> Unit = {},
        onDelete: () -> Unit
    ) {
        val dialog = Dialog(activity, R.style.RoundedCornersDialog)
        val binding = DialogWarningDeleteBinding.inflate(activity.layoutInflater)
        dialog.setContentView(binding.root)
        val width = activity.resources.displayMetrics.widthPixels
        binding.apply {
            txtStatus.text = status
            txtDescription.text = statement
            btnCancel.text = btnText1
            btnDelete.text = btnText2
            btnCancel.setOnClickListener {
                dialog.dismiss()
                onCancel()
            }
            btnDelete.setOnClickListener {
                dialog.dismiss()
                onDelete()
            }
        }
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }


}
