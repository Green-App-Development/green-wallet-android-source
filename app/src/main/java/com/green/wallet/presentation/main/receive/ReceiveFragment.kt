package com.green.wallet.presentation.main.receive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.green.wallet.R
import com.green.wallet.databinding.FragmentReceiveBinding
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.send.NetworkAdapter
import com.green.wallet.presentation.viewBinding
import com.green.wallet.presentation.tools.VLog
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class ReceiveFragment : DaggerFragment() {

    companion object {
        const val NETWORK_TYPE_KEY = "coin_type"
        const val FINGERPRINT_KEY = "fingerprint"
    }

    private val binding by viewBinding(FragmentReceiveBinding::bind)

    @Inject
    lateinit var effect: AnimationManager

    private lateinit var networkAdapter: NetworkAdapter


    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: ReceiveViewModel by viewModels { viewModelFactory }


    var curNetworkType: String = ""
    lateinit var receiveViewPagerAdapter: ReceiveViewPagerAdapter
    private var curFingerPrint: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            curNetworkType = it.getString(NETWORK_TYPE_KEY)!!
            if (it.get(FINGERPRINT_KEY) != null)
                curFingerPrint = it.get(FINGERPRINT_KEY) as Long
        }
        VLog.d("FingerPrint on Receive Fragment : $curFingerPrint and CurNetworkType : $curNetworkType")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_receive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        initViewDetails()
        registerBtnClicks()
        determineOpenOneWallet()
        intViewPagerAdapterWithList(curNetworkType, fingerPrint = curFingerPrint)
        updateNetworkType(networkType = curNetworkType)
        initNetworkTypeAdapter()
    }

    private fun determineOpenOneWallet() {
        if (curFingerPrint != null) {
            chosenNetworkRel.isEnabled = false
            imgIconSpinner.visibility = View.GONE
        }
    }

    private fun intViewPagerAdapterWithList(networkType: String, fingerPrint: Long?) {
        lifecycleScope.launch {

            val walletLisByNetworkType =
                viewModel.getWalletListHomeIsAddedByNetworkType(
                    networkType = networkType,
                    fingerPrint
                )
            VLog.d("Size of receiving walletListHomeIsAdded by networkType  ->  ${walletLisByNetworkType.size}")
            binding.apply {
                txtAddress.visibility = View.VISIBLE
                relCopy.visibility = View.VISIBLE
            }
            initViewPagerInitialize(walletLisByNetworkType)
        }
    }

    private fun initViewPagerInitialize(walletList: List<Wallet>) {

        VLog.d("WalletList Size for view pager -> ${walletList.size} ")

        receiveViewPagerAdapter = ReceiveViewPagerAdapter(
            walletList,
            this@ReceiveFragment.requireActivity()
        )
        binding.apply {
            viewPagerReceive.adapter = receiveViewPagerAdapter
            pageIndicator.count = walletList.size
            viewPagerReceive.addOnPageChangeListener(object :
                ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    pageIndicator.setSelected(position)
                    if (position < walletList.size)
                        txtAddressWallet.setText(
                            walletList[position].address
                        )
                }

                override fun onPageScrollStateChanged(state: Int) {

                }

            })
            if (walletList.isNotEmpty())
                txtAddressWallet.setText(
                    walletList[0].address
                )
        }
    }

    private fun registerBtnClicks() {

        binding.btnShare.setOnClickListener {
            it.startAnimation(effect.getBtnEffectAnimation())
            curActivity().launchingIntentForSendingWalletAddress(binding.txtAddressWallet.text.toString())
        }

        binding.backLayout.setOnClickListener {
            it.startAnimation(effect.getBtnEffectAnimation())
            curActivity().popBackStackOnce()
        }

        binding.relCopy.setOnClickListener {
            val clipBoard =
                curActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                "label",
                binding.txtAddressWallet.text.toString()
            )
            clipBoard.setPrimaryClip(clip)
            binding.relCopied.visibility = View.VISIBLE
            lifecycleScope.launch {
                delay(2000)
                kotlin.runCatching {
                    binding.relCopied.visibility = View.GONE
                }.onFailure {
                    VLog.d("Exception in changing relCopied visibility")
                }
            }
        }

        binding.chosenNetworkRel.setOnClickListener {
            binding.networkSpinner.performClick()
        }


    }


    private fun initNetworkTypeAdapter() {
        lifecycleScope.launch {

            val distinctNetworkTypes =
                viewModel.getDistinctNetworkTypeValues()
                    .toMutableList()

            networkAdapter =
                NetworkAdapter(
                    curActivity(),
                    distinctNetworkTypes
                )
            binding.networkSpinner.adapter = networkAdapter
            binding.networkSpinner.setSelection(distinctNetworkTypes.indexOf(curNetworkType))
            binding.networkSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        VLog.d("Choose spinner for network on receive Fragme")
                        networkAdapter.selectedPosition = p2
                        intViewPagerAdapterWithList(
                            networkAdapter.dataOptions[p2],
                            curFingerPrint
                        )
                        updateNetworkType(networkAdapter.dataOptions[p2])
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }
        }
    }


    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
    }

    private fun curActivity() = requireActivity() as MainActivity


    fun updateNetworkType(networkType: String) {
        binding.txtChosenNetwork.setText("$networkType")
    }


}
