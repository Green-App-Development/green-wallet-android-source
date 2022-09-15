package com.android.greenapp.presentation.main.importtoken

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentImportTokenBinding
import com.android.greenapp.domain.entity.Token
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.custom.hidePublicKey
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.viewBinding
import com.android.greenapp.presentation.tools.Resource
import com.android.greenapp.presentation.tools.getStringResource
import com.example.common.tools.VLog
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 04.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ImportTokenFragment : DaggerFragment(), TokenAdapter.TokenAdapterListener {

    private val binding by viewBinding(FragmentImportTokenBinding::bind)

    @Inject
    lateinit var effect: AnimationManager

    @Inject
    lateinit var dialog: DialogManager

    private lateinit var tokenAdapter: TokenAdapter

    private var jobViewGone: Job? = null

    private var importTokenJob: Job? = null
    private var tokenSearchJob: Job? = null

    @Inject
    lateinit var factory: ViewModelFactory
    private val viewModel: ImportTokenViewModel by viewModels { factory }


    companion object {
        const val FINGER_PRINT_KEY = "finger_print_key"
        const val NETWORK_TYPE_KEY = "network_type_key"
    }

    var curFingerPrint: Long? = null
    var curNetworkType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            curFingerPrint = it.getLong(FINGER_PRINT_KEY)
            curNetworkType = it.getString(NETWORK_TYPE_KEY, "")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_import_token, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerBtnListeners()
        initRecView()
        searchTokenList(null)
        initViewDetails()
    }

    @SuppressLint("SetTextI18n")
    private fun initViewDetails() {
        binding.networkFingerPrint.setText(
            "${curNetworkType.split(" ")[0]}  ${
                hidePublicKey(
                    curFingerPrint!!
                )
            }"
        )
    }

    private fun searchTokenList(nameCode: String?) {
        tokenSearchJob?.cancel()
        tokenSearchJob = lifecycleScope.launch {
            val list = viewModel.getTokenListAndSearch(curFingerPrint!!, nameCode)
            tokenAdapter.updateTokenList(list)
        }
    }

    private fun initRecView() {
        tokenAdapter = TokenAdapter(this, curActivity())
        binding.recViewTokens.apply {
            adapter = tokenAdapter
            layoutManager = LinearLayoutManager(curActivity())
        }
    }

    private fun registerBtnListeners() {

        binding.backLayout.setOnClickListener {
            it.startAnimation(effect.getBtnEffectAnimation())
            curActivity().popBackStackOnce()
        }

        binding.edtSearch.addTextChangedListener {
            if (it == null) return@addTextChangedListener
            tokenAdapter.searching = true
            searchTokenList(it.toString())
            lifecycleScope.launch {
                delay(1000)
                tokenAdapter.searching = false
            }
        }

    }

    private fun curActivity() = requireActivity() as MainActivity

    override fun importToken(
        added: Boolean,
        hash: String,
        switchImport: SwitchCompat,
        token: Token
    ) {
        VLog.d("Importing token $hash, added : $added, token : $token")
        val fingerPrint = curFingerPrint!!
        switchImport.isChecked = added
        viewModel.importToken(hash, fingerPrint, added)
        if (added) {
            binding.relAddedHome.apply {
                visibility = View.VISIBLE
                makeViewGone(this)
            }
        }
    }

    private fun makeViewGone(relAddedHome: RelativeLayout) {
        jobViewGone?.cancel()
        jobViewGone = lifecycleScope.launch {
            delay(2000)
            relAddedHome.visibility = View.GONE
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        importTokenJob?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        importTokenJob?.cancel()
    }


}