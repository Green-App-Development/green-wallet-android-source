package com.android.greenapp.presentation.main.createnewwallet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.databinding.ProgressWalletCreatingBinding
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.viewBinding
import com.android.greenapp.presentation.tools.Resource
import com.android.greenapp.presentation.tools.getColorResource
import dagger.android.support.DaggerDialogFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 13.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ProgressCreatingWalletFragment : DaggerDialogFragment() {


    private val binding: ProgressWalletCreatingBinding by viewBinding(ProgressWalletCreatingBinding::bind)

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: NewWalletViewModel by viewModels { viewModelFactory }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.progress_wallet_creating, container, false)
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addingThreeDotsEnd()
        initStatusBarColor()
    }


    private fun initStatusBarColor() {
        dialog?.apply {
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor =
                requireActivity().getColorResource(R.color.status_bar_clr_progress_create_wallet)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun addingThreeDotsEnd() {
        binding.apply {
            val curText = txtForCreatingANewWalletDescription.text.toString()
            txtForCreatingANewWalletDescription.text = "$curText..."
        }
    }

    private fun curActivity() = requireActivity() as MainActivity


}