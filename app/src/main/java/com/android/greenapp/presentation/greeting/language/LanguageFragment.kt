package com.android.greenapp.presentation.greeting.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.greenapp.R
import com.android.greenapp.data.network.dto.greenapp.language.LanguageItem
import com.android.greenapp.databinding.FragmentLanguageBinding
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.greeting.GreetingActivity
import com.android.greenapp.presentation.greeting.GreetingViewModel
import com.android.greenapp.presentation.viewBinding
import com.android.greenapp.presentation.tools.Resource
import com.example.common.tools.VLog
import dagger.android.support.DaggerFragment
import dev.b3nedikt.reword.Reword
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
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
    private val greetingViewModel: GreetingViewModel by viewModels { viewModelFactory }


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
        greetingViewModel.getAllLanguageList()
        VLog.d("GreetingViewModel LanguageFragment on ViewCreated : $greetingViewModel")
    }

    private fun downLoadingLang() {
        job = lifecycleScope.launch {
            greetingViewModel.downloadingLang.collect {
                it?.let {
                    when (it.state) {
                        Resource.State.ERROR -> {
                            dialogManager.hideProgress()
                            dialogManager.showServerErrorDialog(curActivity()) {

                            }
                        }
                        Resource.State.LOADING -> {

                        }
                        Resource.State.SUCCESS -> {
                            dialogManager.hideProgress()
                            updateView()
                            curActivity().move2TermsFragment()
                        }
                    }
                }
            }
        }
    }

    private fun updateView() {
        val rootView: View = curActivity().window.decorView.findViewById(android.R.id.content)
        Reword.reword(rootView)
    }


    private fun initLangRecyclerView() {
        langAdapter = LanguageAdapter(object : LanguageAdapter.LanguageClicker {
            override fun onLanguageClicked(langItem: LanguageItem) {
                greetingViewModel.downloadLanguage(langItem.code)
                downLoadingLang()
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
                        VLog.d("Erros status to get LangList")
                        curActivity().apply {
                            dialogManager.hideProgress()
                            dialogManager.showServerErrorDialog(curActivity()) {

                            }
                        }
                    }
                    Resource.State.SUCCESS -> {
                        VLog.d("Success Status Language List : ${it.data}")
                        langAdapter.updateLanguageList(it.data ?: mutableListOf())
                        dialogManager.hideProgress()
                    }
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

    private fun curActivity() = requireActivity() as GreetingActivity


    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }


}