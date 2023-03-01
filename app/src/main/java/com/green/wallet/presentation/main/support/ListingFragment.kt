package com.green.wallet.presentation.main.support

import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.green.wallet.presentation.tools.VLog
import com.example.common.tools.validateEmail
import com.green.wallet.R
import com.green.wallet.data.network.dto.support.ListingPost
import com.green.wallet.databinding.FragmentListingBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.CustomSpinner
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.manageExceptionDialogsForRest
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.send.NetworkAdapter
import com.green.wallet.presentation.tools.*
import dagger.android.support.DaggerDialogFragment
import kotlinx.android.synthetic.main.fragment_listing.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class ListingFragment : DaggerDialogFragment() {

	private lateinit var binding: FragmentListingBinding

	@Inject
	lateinit var animManage: AnimationManager

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception thrown : ${ex}")
	}

	@Inject
	lateinit var dialogManager: DialogManager

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val viewModel: SupportViewModel by viewModels { viewModelFactory }

	private var listingJob: Job? = null

	private val enableBtnSend = mutableSetOf<Int>()

	private var firstTimeNotSetToEdt = true

	private var job: Job? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentListingBinding.inflate(layoutInflater)
		curActivity().listingFragmentView = binding.root
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initStatusBarColor()
		registerClicks()
		initSpinnerBlockChain()

	}


	private fun initSpinnerBlockChain() {

		val adapter = NetworkAdapter(curActivity(), listOf("Chia", "Chives"))

		binding.apply {

			imgArrow.setOnClickListener {
				spinnerBlockchain.performClick()
				animManage.rotateBy180Forward(it,curActivity())
				txtBlockChain.visibility = View.VISIBLE
				edtBlockChain.setText(if (adapter.selectedPosition == 0) CHIA else CHIVES)
				firstTimeNotSetToEdt = false
			}

			spinnerBlockchain.adapter = adapter

			spinnerBlockchain.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					VLog.d("CurSelected Position : $p2")
					adapter.selectedPosition = p2
					if (!firstTimeNotSetToEdt) {
						edtBlockChain.setText(if (adapter.selectedPosition == 0) CHIA else CHIVES)
					}
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {

				}

			}

			spinnerBlockchain.setSpinnerEventsListener(object :
				CustomSpinner.OnSpinnerEventsListener {
				override fun onSpinnerOpened(spin: Spinner?) {
					view6.setBackgroundColor(curActivity().getColorResource(R.color.green))
				}

				override fun onSpinnerClosed(spin: Spinner?) {
					animManage.rotateBy180Backward(imgArrow,curActivity())
					view6.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
				}

			})

		}

	}

	private fun registerClicks() {
		binding.apply {

			backLayout.setOnClickListener {
				curActivity().popBackStackOnce()
			}

			focusChangeWhenEnteredEdt(
				edtName,
				txtName,
				view2,
				curActivity().getString(R.string.listing_request_name)
			)

			focusChangeWhenEnteredEdt(
				edtEmail,
				txtEmail,
				view3,
				curActivity().getString(R.string.listing_request_e_mail)
			)

			focusChangeWhenEnteredEdt(
				edtProjectName,
				txtProjectName,
				view4,
				curActivity().getString(R.string.listing_request_project_name)
			)

			focusChangeWhenEnteredEdt(
				edtDescriptionProject,
				txtDescriptionProject,
				view5,
				curActivity().getString(R.string.listing_request_project_description)
			)

			focusChangeWhenEnteredEdt(
				edtTwitter,
				txtTwitter,
				view7,
				curActivity().getString(R.string.listing_request_twitter)
			)

			checkboxAgree.setOnCheckedChangeListener { btn, checked ->
				btnSend.isEnabled = enableBtnSend.size == 5 && checked
			}

			btnSend.setOnClickListener {
				it.startAnimation(animManage.getBtnEffectAnimation())
				sendingListingRequest()
			}


			mutableListOf(
				edtName,
				edtEmail,
				edtBlockChain,
				edtDescriptionProject,
				edtProjectName
			).forEach { enabledBtnSendWhenFilledEdts(it) }

			edtDescriptionProject.setImeOptions(EditorInfo.IME_ACTION_DONE);
			edtDescriptionProject.setRawInputType(InputType.TYPE_CLASS_TEXT);

			checkboxText.text =
				Html.fromHtml(curActivity().getStringResource(R.string.personal_data_agreement_chekbox))
			checkboxText.setMovementMethod(LinkMovementMethod.getInstance())
		}
	}

	private fun focusChangeWhenEnteredEdt(edt: EditText, txt: TextView, line: View, hint: String) {
		edt.setOnFocusChangeListener { view, focus ->
			if (focus) {
				txt.visibility = View.VISIBLE
				edt.hint = ""
				line.setBackgroundColor(curActivity().getColorResource(R.color.green))
			} else if (edt.text.toString().isEmpty()) {
				txt.visibility = View.INVISIBLE
				edt.hint =
					hint
			}
			if (!focus) {
				line.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
			}
		}

	}

	private fun enabledBtnSendWhenFilledEdts(edt: EditText) {
		edt.addTextChangedListener {
			if (it.isNullOrEmpty()) {
				enableBtnSend.remove(edt.hashCode())
			} else
				enableBtnSend.add(edt.hashCode())
			btnSend.isEnabled = checkboxAgree.isChecked && enableBtnSend.size == 5
		}
	}

	private fun sendingListingRequest() {
		if (!validateEmail(binding.edtEmail.text.toString())) {
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
			return
		} else {

			val listingPost = getListingPost()
			listingJob?.cancel()
			listingJob = lifecycleScope.launch {
				val res = viewModel.postListing(listingPost)
				when (res.state) {
					Resource.State.LOADING -> {

					}
					Resource.State.SUCCESS -> {
						curActivity().apply {
							dialogManager.showSuccessDialog(
								this,
								getStringResource(R.string.pop_up_sent_title),
								getStringResource(R.string.pop_up_sent_a_question_description),
								getStringResource(R.string.ready_btn)
							) {
								curActivity().popBackStackOnce()
							}
						}
					}
					Resource.State.ERROR -> {
						manageExceptionDialogsForRest(curActivity(), dialogManager, res.error)
					}
				}
			}
		}
	}

	private fun getListingPost(): ListingPost {

		val curBlockChain = edtBlockChain.text.toString()
		val trimedBlockChain =
			if (curBlockChain.lowercase().contains("chia")) "chia " else "chives"
		var listingPost: ListingPost?
		binding.apply {
			listingPost = ListingPost(
				edtName.text.toString(),
				edtEmail.text.toString(),
				edtProjectName.text.toString(),
				edtDescriptionProject.text.toString(),
				trimedBlockChain,
				edtTwitter.text.toString()
			)
		}
		return listingPost!!
	}


	private fun initStatusBarColor() {
		dialog?.apply {
			window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window?.statusBarColor =
				curActivity().getColorResource(R.color.primary_app_background)
		}
	}


	override fun getTheme(): Int {
		return R.style.DialogTheme
	}

	private fun curActivity() = requireActivity() as MainActivity


}
