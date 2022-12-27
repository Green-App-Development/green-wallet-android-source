package com.green.wallet.presentation.main.language

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.green.wallet.R
import com.green.wallet.data.network.GreenAppService
import com.green.wallet.data.network.dto.greenapp.lang.LanguageItemDto
import com.green.wallet.databinding.FragmentMainlanguageBinding
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.onboard.language.LanguageAdapter
import com.green.wallet.presentation.tools.JsonHelper
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.viewBinding
import com.example.common.tools.VLog
import com.google.gson.Gson
import dagger.android.support.DaggerDialogFragment
import dev.b3nedikt.reword.Reword
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
		initStatusBarColor()
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
							val exceptionType = it.error
							manageExceptionDialogsForRest(
								curActivity(),
								dialogManager,
								exceptionType
							)
						}
						Resource.State.LOADING -> {

						}
						Resource.State.SUCCESS -> {
							dialogManager.hidePrevDialogs()
							updateView()
						}
					}
				}
			}
		}
	}

	private fun initLangRecyclerView() {
		langAdapter = LanguageAdapter(object : LanguageAdapter.LanguageClicker {
			override fun onLanguageClicked(langItem: LanguageItemDto) {
				downloadingLang(langItem.code)
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
					}
					Resource.State.ERROR -> {
						VLog.d("Error status to get LangList")
						manageExceptionDialogsForRest(curActivity(), dialogManager, it.error)
					}
					Resource.State.SUCCESS -> {
						VLog.d("Success Status Language List : ${it.data}")
						langAdapter.updateLanguageList(it.data ?: mutableListOf())
						dialogManager.hidePrevDialogs()
					}
				}
			}
		}

	}

	private var jobDownloadingLang: Job? = null

	private fun downloadingLang(code: String) {
		if (jobDownloadingLang != null && jobDownloadingLang!!.isActive)
			return
		jobDownloadingLang = lifecycleScope.launch {
			val res = mainLanguageViewModel.downloadLanguage(code)
			when (res.state) {
				Resource.State.ERROR -> {
					manageExceptionDialogsForRest(curActivity(), dialogManager, res.error)
				}
				Resource.State.SUCCESS -> {
					updateView()
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

	override fun onResume() {
		super.onResume()
		mainLanguageViewModel.getAllLanguageList(5)
	}


	override fun getTheme(): Int {
		return R.style.DialogTheme
	}

	private fun curActivity() = requireActivity() as MainActivity

}
