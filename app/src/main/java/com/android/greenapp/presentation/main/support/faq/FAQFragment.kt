package com.android.greenapp.presentation.main.support.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentFaqBinding
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.main.support.SupportViewModel
import com.android.greenapp.presentation.tools.Resource
import com.android.greenapp.presentation.tools.getColorResource
import dagger.android.support.DaggerDialogFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.recyclerview.widget.SimpleItemAnimator


/**
 * Created by bekjan on 02.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class FAQFragment : DaggerDialogFragment() {


    private lateinit var binding: FragmentFaqBinding
    private lateinit var faqAdapter: FAQAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: SupportViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var dialogManager: DialogManager

    private var faqJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFaqBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStatusBarColor()
        initFAQAdapter()
        registerClicks()
    }

    private fun registerClicks() {
        binding.apply {

            backLayout.setOnClickListener {
                curActivity().popBackStackOnce()
            }


        }
    }

    private fun initFAQAdapter() {
        faqAdapter = FAQAdapter(curActivity())
        binding.recViewFaq.adapter = faqAdapter

        faqJob?.cancel()
        (binding.recViewFaq.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        faqJob = lifecycleScope.launch {
            val res = viewModel.getFAQQuestionAnswers()
            when (res.state) {

                Resource.State.SUCCESS -> {
                    val faqList = res.data!!
                    faqAdapter.updateQuestionList(faqList)
                }
                Resource.State.ERROR -> {
                    dialogManager.showServerErrorDialog(curActivity()) {

                    }
                }

                Resource.State.LOADING -> {

                }

            }
        }

    }


    private fun curActivity() = requireActivity() as MainActivity


    private fun initStatusBarColor() {
        dialog?.apply {
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = curActivity().getColorResource(R.color.primary_app_background)
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

}