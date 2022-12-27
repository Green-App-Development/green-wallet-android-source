package com.green.wallet.presentation.onboard.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.green.wallet.R
import com.green.wallet.databinding.FragmentLanguageBinding
import com.green.wallet.data.network.dto.greenapp.lang.LanguageItemDto
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.manageExceptionDialogsForRest
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.onboard.OnBoardActivity
import com.green.wallet.presentation.onboard.OnBoardViewModel
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.viewBinding
import com.example.common.tools.VLog
import dagger.android.support.DaggerFragment
import dev.b3nedikt.reword.Reword
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Created by bekjan on 08.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class LanguageFragment : DaggerFragment() {

	private val binding: FragmentLanguageBinding by viewBinding(FragmentLanguageBinding::bind)

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val greetingViewModel: OnBoardViewModel by viewModels { viewModelFactory }


	@Inject
	lateinit var dialogManager: DialogManager


	private lateinit var langAdapter: LanguageAdapter

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.e("Exception in getting languageItemList : ${ex.message}")
	}

	private var job: Job? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_language, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initLangRecyclerView()
		VLog.d("GreetingViewModel LanguageFragment on ViewCreated : $greetingViewModel")
	}

	private fun updateView() {
		val rootView: View = curActivity().window.decorView.findViewById(android.R.id.content)
		Reword.reword(rootView)
	}

	override fun onStart() {
		super.onStart()
		VLog.d("On  Start on  Start Fragment")
	}


	override fun onResume() {
		super.onResume()
		VLog.d("On Resume on languageFragment")
		greetingViewModel.getAllLanguageList(5)

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
			greetingViewModel.languageList.collect {
				when (it.state) {
					Resource.State.LOADING -> {
						VLog.d("Loading Status to get LangList")
					}
					Resource.State.ERROR -> {
						VLog.d("Error status to get LangList ")
						curActivity().apply {
							manageExceptionDialogsForRest(curActivity(), dialogManager, it.error)
						}
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
			val res = greetingViewModel.downloadLanguage(code)
			when (res.state) {
				Resource.State.ERROR -> {
					manageExceptionDialogsForRest(curActivity(), dialogManager, res.error)
				}
				Resource.State.SUCCESS -> {
					updateView()
					curActivity().move2TermsFragment()
				}
			}
		}
	}


	private fun gettingStringKeysOnly(resHashMap: HashMap<String, String>) {
		val strings = Array(10) { "" }
		var cur = ""
		var index = 0
		var counter = 0
		val onlyKeys = resHashMap.map { it.key }.toList().sorted()
		for (key in onlyKeys) {
			cur += "<string name=\"$key\"/>"
			counter++
			if (counter == 20) {
				strings[index] = cur
				index++
				counter = 0
				cur = ""
			}
		}
		for (k in strings) {
			VLog.d("Resource : $k")
		}
	}

	private fun curActivity() = requireActivity() as OnBoardActivity


	override fun onDestroyView() {
		super.onDestroyView()
		job?.cancel()
	}


}
