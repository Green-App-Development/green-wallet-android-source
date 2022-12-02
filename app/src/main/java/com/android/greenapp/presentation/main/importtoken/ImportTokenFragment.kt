package com.android.greenapp.presentation.main.importtoken

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentImportTokenBinding
import com.android.greenapp.domain.domainmodel.Token
import com.android.greenapp.domain.domainmodel.Wallet
import com.android.greenapp.presentation.App
import com.android.greenapp.presentation.custom.*
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.android.greenapp.presentation.tools.Resource
import com.android.greenapp.presentation.viewBinding
import com.example.common.tools.VLog
import dagger.android.support.DaggerFragment
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineExceptionHandler
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
	lateinit var dialogManager: DialogManager

	private lateinit var tokenAdapter: TokenAdapter

	private var jobViewGone: Job? = null

	private var importTokenJob: Job? = null
	private var tokenSearchJob: Job? = null

	@Inject
	lateinit var factory: ViewModelFactory
	private val viewModel: ImportTokenViewModel by viewModels { factory }

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception in setting importing token : $ex")
	}


	companion object {
		const val FINGER_PRINT_KEY = "finger_print_key"
		const val NETWORK_TYPE_KEY = "network_type_key"
		const val MAIN_PUZZLE_HASH = "main_puzzle_hash"
		const val ADDRESS_KEY = "address_key"
	}

	var curFingerPrint: Long? = null
	var curNetworkType: String = ""
	var address: String = ""

	lateinit var wallet: Wallet


	lateinit var methodChannel: MethodChannel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			curFingerPrint = it.getLong(FINGER_PRINT_KEY)
			curNetworkType = it.getString(NETWORK_TYPE_KEY, "")
			address = it.getString(ADDRESS_KEY, "")
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
		methodChannel = MethodChannel(
			(curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
			METHOD_CHANNEL_GENERATE_HASH
		)
		lifecycleScope.launch {
			wallet = viewModel.getWalletByAddress(address = address)
		}
	}

	private fun searchTokenList(nameCode: String?) {
		tokenSearchJob?.cancel()
		tokenSearchJob = lifecycleScope.launch(handler) {
			val res = viewModel.getTokenListAndSearch(curFingerPrint!!, nameCode)
			when (res.state) {
				Resource.State.SUCCESS ->
					tokenAdapter.updateTokenList(res.data!!)
				Resource.State.ERROR -> manageExceptionDialogsForRest(
					curActivity(),
					dialogManager = dialogManager,
					res.error
				)
			}
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
		switchImport: SwitchCompat,
		token: Token
	) {
		VLog.d("Importing token added : $added, token : $token")
		switchImport.isChecked = added
		token.imported = added
		if (added) {
			binding.relAddedHome.apply {
				visibility = View.VISIBLE
				makeViewGone(this)
			}
			val map = hashMapOf<String, String>()
			map["puzzle_hashes"] = convertListToStringWithSpace(wallet.puzzle_hashes)
			map["asset_id"] = token.hash
			methodChannel.setMethodCallHandler { method, calLBack ->
				if (method.method == "generate_outer_hash") {
					val args = method.arguments as HashMap<*, *>
					val outer_hashes = args[token.hash]!! as List<String>
					viewModel.importToken(token.hash, address, added, outer_hashes)
				}
			}
			methodChannel.invokeMethod("generatewrappedcatpuzzle", map)
		} else
			viewModel.importToken(token.hash, address, added, listOf())
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
