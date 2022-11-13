package com.android.greenapp.presentation.main.address

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentAddressListBinding
import com.android.greenapp.domain.domainmodel.Address
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.viewBinding
import com.example.common.tools.VLog
import com.android.greenapp.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


/**
 * Created by bekjan on 19.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class AddressFragment : DaggerFragment(), AddressAdapter.EditedOpenListener {

	private val binding by viewBinding(FragmentAddressListBinding::bind)

	@Inject
	lateinit var anim: AnimationManager

	@Inject
	lateinit var dialogManager: DialogManager

	private var addressAdapter: AddressAdapter? = null

	private var shouldBeClickable: Boolean = false

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val addressViewModel: AddressViewModel by viewModels { viewModelFactory }

	companion object {
		const val SHOULD_BE_CLICKABLE_KEY = "should_be_clickable"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			shouldBeClickable = it.getBoolean(SHOULD_BE_CLICKABLE_KEY, false)
			curActivity().shouldGoBackHomeFragmentFromAddress = !shouldBeClickable
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return layoutInflater.inflate(R.layout.fragment_address_list, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerClicks()
		initRecAddressView()
		searchFunctionality()
	}

	private fun searchFunctionality() {
		binding.edtSearchAddress.addTextChangedListener {
			if (it == null) return@addTextChangedListener
			addressViewModel.searchAddressByName(name = it.toString().trim())
		}
	}


	private fun initRecAddressView() {
		addressAdapter = AddressAdapter(this, anim = anim, shouldBeClickable)
		binding.recViewAddresses.apply {
			layoutManager = LinearLayoutManager(curActivity())
			adapter = addressAdapter
		}
		lifecycleScope.launchWhenStarted {
			addressViewModel.address_items.collect {
				it?.let {
					VLog.d("Getting List of Addresses : $it")
					addressAdapter!!.updateAddressList(it)
				}
			}
		}
	}

	private fun registerClicks() {

		binding.relAddAddress.setOnClickListener {
			curActivity().move2EditAddressFragment(null)
		}

	}


	private fun curActivity() = requireActivity() as MainActivity

	override fun onDelete(itemAddress: Address) {
		curActivity().apply {
			dialogManager.showFailureDialog(
				this,
				getStringResource(R.string.address_book_pop_up_delete_title),
				getStringResource(R.string.address_book_pop_up_delete_description),
				getStringResource(R.string.confirm_btn)
			) {
				curActivity().apply {
					dialogManager.showSuccessDialog(
						this,
						getStringResource(R.string.adress_book_pop_up_removed_title),
						getStringResource(R.string.adress_book_pop_up_removed_description),
						getStringResource(R.string.ready_btn)
					) {
						addressViewModel.deleteAddressItem(itemAddress)
					}
				}
			}
		}
	}

	override fun onEdit(itemAddress: Address) {
		curActivity().move2EditAddressFragment(itemAddress)
	}

	override fun onSend(itemAddress: Address) {
		val intent = Intent(Intent.ACTION_SEND)
		val content = itemAddress.address
		intent.type = "text/plain"
		intent.putExtra(
			Intent.EXTRA_SUBJECT,
			getString(R.string.address_wallet)
		)
		intent.putExtra(Intent.EXTRA_TEXT, content)
		requestSendingTextViaMessengers.launch(intent)

	}

	override fun onAddressClicked(address: String) {
		VLog.d("Saving address to send money : $address")
		curActivity().mainViewModel.saveChosenAddress(address)
		curActivity().popBackStackOnce()
	}

	private val requestSendingTextViaMessengers =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

		}

	override fun onDestroyView() {
		super.onDestroyView()
	}


}
