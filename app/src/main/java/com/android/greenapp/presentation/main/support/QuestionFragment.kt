package com.android.greenapp.presentation.main.support

import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.data.network.dto.support.QuestionPost
import com.android.greenapp.databinding.FragmentQuestionBinding
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.custom.isExceptionBelongsToNoInternet
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.Resource
import com.android.greenapp.presentation.tools.getColorResource
import com.android.greenapp.presentation.tools.getStringResource
import com.example.common.tools.*
import dagger.android.support.DaggerDialogFragment
import dev.b3nedikt.restring.Restring
import kotlinx.android.synthetic.main.fragment_listing.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 02.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class QuestionFragment : DaggerDialogFragment() {

    private lateinit var binding: FragmentQuestionBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val supportViewModel: SupportViewModel by viewModels { viewModelFactory }

    private val handler = CoroutineExceptionHandler { _, ex ->
        VLog.d("Exception in setting highlightedGreenWords : $ex")
    }

    @Inject
    lateinit var dialogManager: DialogManager

    private var enableBtn = mutableSetOf<Int>()


    @Inject
    lateinit var effect: AnimationManager

    private var questionJob: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuestionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerClicks()
        initStatusBarColor()
        //need to fix
        highLightWordsGreenInTermsProcessing()
    }

    private fun registerClicks() {


        binding.apply {

            backLayout.setOnClickListener {
                curActivity().popBackStackOnce()
            }

            focusChangeEdts(
                edtName,
                txtName,
                view2,
                curActivity().getString(R.string.ask_a_question_name)
            )

            focusChangeEdts(
                edtEmail,
                txtEmail,
                view3,
                curActivity().getString(R.string.ask_a_question_e_mail)
            )

            focusChangeEdts(
                edtQuestion,
                txtQuestion,
                view4,
                curActivity().getString(R.string.ask_a_question_question)
            )

            addTextChangeEventListenerEachEdts(edtName)
            addTextChangeEventListenerEachEdts(edtQuestion)
            addTextChangeEventListenerEachEdts(edtEmail)

            edtQuestion.setHorizontallyScrolling(false)

            checkboxAgree.setOnCheckedChangeListener { p0, p1 ->
                btnSend.isEnabled = enableBtn.size == 3 && p1
            }

            btnSend.setOnClickListener {
                it.startAnimation(effect.getBtnEffectAnimation())
                val email = edtEmail.text.toString()
                if (!validateEmail(email)) {
                    edtEmail.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
                    txtEmail.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
                    txtAddressDontExistWarning.visibility = View.VISIBLE
                    view3.setBackgroundColor(curActivity().getColorResource(R.color.red_mnemonic))
                    lifecycleScope.launch {
                        delay(2000)
                        edtEmail.setTextColor(curActivity().getColorResource(R.color.secondary_text_color))
                        txtEmail.setTextColor(curActivity().getColorResource(R.color.green))
                        txtAddressDontExistWarning.visibility = View.GONE
                        view3.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
                    }
                    return@setOnClickListener
                }
                requestSendingQuestion()
            }

            edtQuestion.setImeOptions(EditorInfo.IME_ACTION_DONE);
            edtQuestion.setRawInputType(InputType.TYPE_CLASS_TEXT);

        }
    }

    private fun requestSendingQuestion() {
        questionJob?.cancel()
        questionJob = lifecycleScope.launch {
            binding.apply {
                supportViewModel.postQuestion(
                    QuestionPost(
                        edtEmail.text.toString(),
                        edtName.text.toString(),
                        edtQuestion.text.toString()
                    ), 3
                )
            }
            supportViewModel.postQuestion.collect { res ->
                when (res?.state) {
                    Resource.State.SUCCESS -> {
                        curActivity().apply {
                            dialogManager.showSuccessDialog(
                                this,
                                getStringResource(R.string.pop_up_sent_title),
                                getStringResource(R.string.pop_up_sent_a_question_description),
                                getStringResource(R.string.ready_btn)
                            ) {
                                popBackStackOnce()
                            }
                        }
                    }
                    Resource.State.ERROR -> {
                        val error = res.error
                        if (isExceptionBelongsToNoInternet(error!!)) {
                            dialogManager.showNoInternetTimeOutExceptionDialog(curActivity()) {

                            }
                        } else {
                            curActivity().apply {
                                dialogManager.showFailureDialog(
                                    this, getStringResource(R.string.pop_up_failed_error_title),
                                    getStringResource(R.string.pop_up_failed_error_description),
                                    getStringResource(R.string.pop_up_failed_error_return_btn)
                                ) {

                                }
                            }
                        }
                    }
                    Resource.State.LOADING -> {

                    }
                }
            }
        }
    }

    private fun addTextChangeEventListenerEachEdts(edt: EditText) {
        edt.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                enableBtn.remove(edt.hashCode())
            } else {
                enableBtn.add(edt.hashCode())
            }
            binding.btnSend.isEnabled =
                enableBtn.size == 3 && binding.checkboxAgree.isChecked
        }
    }

    private fun focusChangeEdts(
        edt: EditText,
        txt: TextView,
        lineView: View,
        hint: String
    ) {
        edt.setOnFocusChangeListener { view, focus ->
            if (focus) {
                txt.visibility = View.VISIBLE
                edt.hint = ""
                lineView.setBackgroundColor(curActivity().getColorResource(R.color.green))
            } else if (edt.text.toString().isEmpty()) {
                txt.visibility = View.INVISIBLE
                edt.hint = hint
            }
            if (!focus) {
                lineView.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
            }
        }

    }


    private fun initStatusBarColor() {
        dialog?.apply {
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor =
                curActivity().getColorResource(R.color.primary_app_background)
        }
    }


    private fun highLightWordsGreenInTermsProcessing() {
        lifecycleScope.launch(handler) {
            val startEnd =
                getStartingIndexWordCountToHighlightEndingIndex(
                    Restring.locale.toString()
                )
            val ss = SpannableString(binding.checkboxText.text.toString())
            val fcsGreen = ForegroundColorSpan(resources.getColor(R.color.green))
            val underlineSpan = UnderlineSpan()
            ss.setSpan(fcsGreen, startEnd[0], startEnd[1], Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            ss.setSpan(
                underlineSpan,
                startEnd[0],
                startEnd[1],
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            binding.checkboxText.text = ss
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    private fun curActivity() = requireActivity() as MainActivity


}