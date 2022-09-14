package com.android.greenapp.presentation.main.language

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.greenapp.R
import com.android.greenapp.data.network.GreenAppService
import com.android.greenapp.data.network.dto.greenapp.language.LanguageItem
import com.android.greenapp.databinding.FragmentMainlanguageBinding
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.onboard.language.LanguageAdapter
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.viewBinding
import com.android.greenapp.presentation.tools.JsonHelper
import com.android.greenapp.presentation.tools.Resource
import com.example.common.tools.VLog
import com.android.greenapp.presentation.tools.getColorResource
import com.google.gson.Gson
import dagger.android.support.DaggerDialogFragment
import dev.b3nedikt.reword.Reword
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeoutException
import javax.inject.Inject

/**
 * Created by bekjan on 11.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */


class MainLanguageFragment : DaggerDialogFragment() {

	private val binding by viewBinding(FragmentMainlanguageBinding::bind)

	@Inject
	lateinit var effect: AnimationManager

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val mainLanguageViewModel: MainLanguageViewModel by viewModels { viewModelFactory }

	@Inject
	lateinit var jsonHelper: JsonHelper

	@Inject
	lateinit var dialogManager: DialogManager

	@Inject
	lateinit var gson: Gson

	@Inject
	lateinit var languageService: GreenAppService

	private lateinit var langAdapter: LanguageAdapter

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.e("Exception in getting languageItemList : ${ex.message}")
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_mainlanguage, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerClicks()
		initLangRecyclerView()
		mainLanguageViewModel.getAllLanguageList()
		initStatusBarColor()
		downLoadingLang()
	}

	private fun initStatusBarColor() {
		dialog?.apply {
			window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window?.statusBarColor = curActivity().getColorResource(R.color.primary_app_background)
		}
	}

	private fun registerClicks() {
		binding.apply {

			backLayout.setOnClickListener {
				curActivity().popBackStackOnce()
			}


		}
	}

	private fun downLoadingLang() {
		lifecycleScope.launch {
			mainLanguageViewModel.downloadingLang.collect {
				it?.let {
					when (it.state) {
						Resource.State.ERROR -> {
							dialogManager.hideProgress()
							val errorType = it.error
							if (errorType is TimeoutException) {
								dialogManager.showTimeOutException(curActivity()) {

								}
							} else {
								dialogManager.showServerErrorDialog(curActivity()) {

								}
							}
						}
						Resource.State.LOADING -> {

						}
						Resource.State.SUCCESS -> {
							dialogManager.hideProgress()
							updateView()
						}
					}
				}
			}
		}
	}

	private fun initLangRecyclerView() {
		langAdapter = LanguageAdapter(object : LanguageAdapter.LanguageClicker {
			override fun onLanguageClicked(langItem: LanguageItem) {
				mainLanguageViewModel.downloadLanguage(langItem.code)
			}
		}, curActivity())

		binding.apply {
			recViewLang.adapter = langAdapter
			recViewLang.layoutManager = LinearLayoutManager(curActivity())
		}
		lifecycleScope.launch {
			mainLanguageViewModel.languageList.collect {
				when (it.state) {
					Resource.State.LOADING -> {
						VLog.d("Loading Status to get LangList")
						dialogManager.showProgress(curActivity())
					}
					Resource.State.ERROR -> {

						val errorType = it.error

						if (errorType is TimeoutException) {
							dialogManager.hideProgress()
							dialogManager.showTimeOutException(curActivity()) {

							}
						} else {
							dialogManager.hideProgress()
							dialogManager.showServerErrorDialog(curActivity()) {

							}
						}
					}
					Resource.State.SUCCESS -> {
						VLog.d("Language List : ${it.data}")
						langAdapter.updateLanguageList(it.data ?: mutableListOf())
						dialogManager.hideProgress()
					}
				}
			}
		}

	}

	private fun updateView() {
		val rootView: View = curActivity().window.decorView.findViewById(android.R.id.content)
		Reword.reword(rootView)
		Reword.reword(binding.root)
		curActivity().onLanguageChanged()
	}

	override fun onDismiss(dialog: DialogInterface) {
		super.onDismiss(dialog)
		curActivity().onLanguageChanged()
	}


	override fun getTheme(): Int {
		return R.style.DialogTheme
	}

	private fun curActivity() = requireActivity() as MainActivity

}
