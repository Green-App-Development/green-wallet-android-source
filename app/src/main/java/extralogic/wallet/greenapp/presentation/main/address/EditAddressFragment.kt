package extralogic.wallet.greenapp.presentation.main.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentAddressEditBinding
import com.example.common.tools.VLog
import dagger.android.support.DaggerFragment
import extralogic.wallet.greenapp.domain.domainmodel.Address
import extralogic.wallet.greenapp.presentation.custom.AnimationManager
import extralogic.wallet.greenapp.presentation.custom.DialogManager
import extralogic.wallet.greenapp.presentation.custom.EditTextLinesLimiter
import extralogic.wallet.greenapp.presentation.di.factory.ViewModelFactory
import extralogic.wallet.greenapp.presentation.main.MainActivity
import extralogic.wallet.greenapp.presentation.tools.getColorResource
import extralogic.wallet.greenapp.presentation.tools.getStringResource
import extralogic.wallet.greenapp.presentation.viewBinding
import kotlinx.android.synthetic.main.item_address.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


/**
 * Created by bekjan on 24.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class EditAddressFragment : DaggerFragment() {

	private val binding by viewBinding(FragmentAddressEditBinding::bind)

	@Inject
	lateinit var effect: AnimationManager

	@Inject
	lateinit var dialogManager: DialogManager


	companion object {
		const val ADDRESS_ID = "address_key"
	}

	private var addressItem: Address? = null

	private var curAddressStr: String = ""

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val addressViewModel: AddressViewModel by viewModels { viewModelFactory }


	private val setToEnableBtn = mutableSetOf(1)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			val anyAddress = it.get(ADDRESS_ID)
			if (anyAddress is Address) {
				addressItem = anyAddress
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_address_edit, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAddressItem()
		registerClicks()
		edtTextWatcher()
		initLimit3Lines()
		getQrCodeDecoded()
	}

	private fun initAddressItem() {
		if (addressItem != null)
			initViewDetailsAddress(addressItem!!)
		else
			VLog.d("AddressItem is null")
	}


	private fun initViewDetailsAddress(addressItem: Address?) {
		binding.apply {
			edtAddressName.setText(addressItem?.name)
			edtAddressWallet.setText(addressItem?.address)
			btnAddOrEdit.text = curActivity().getStringResource(R.string.save_btn)
			txtAddingEditingContacts.text =
				curActivity().getStringResource(R.string.address_book_edit_contact_title)

			txtAddressName.visibility = View.VISIBLE
			txtAddressWallet.visibility = View.VISIBLE
			if (addressItem!!.description.isNotEmpty()) {
				txtAddressDescription.visibility = View.VISIBLE
				edtAddressDescription.setText(addressItem.description)
			}
			setToEnableBtn.add(2)
			enableBtnAdd()
		}
	}


	private fun edtTextWatcher() {
		binding.apply {


			edtAddressName.addTextChangedListener {
				if (it == null) return@addTextChangedListener
				val value = it.toString().trim()
				if (value.isEmpty())
					setToEnableBtn.remove(1)
				else {
					setToEnableBtn.add(1)
				}

				enableBtnAdd()
			}


			edtAddressWallet.addTextChangedListener {
				if (it == null) return@addTextChangedListener
				val value = it.toString().trim()
				if (value.isEmpty())
					setToEnableBtn.remove(2)
				else {
					setToEnableBtn.add(2)
				}
				enableBtnAdd()
			}

			edtAddressDescription.addTextChangedListener {

			}

		}
	}

	private fun enableBtnAdd() {
		binding.btnAddOrEdit.isEnabled = setToEnableBtn.size >= 2
	}

	private fun initLimit3Lines() {
		binding.edtAddressDescription.addTextChangedListener(
			EditTextLinesLimiter(
				binding.edtAddressDescription,
				3
			)
		)
	}


	private fun registerClicks() {
		binding.apply {
			backLayout.setOnClickListener {
				curActivity().popBackStackOnce()
			}

			imgEdtScan.setOnClickListener {
				requestPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))

			}

			btnAddOrEdit.setOnClickListener {
				it.requestFocus()
				it.startAnimation(effect.getBtnEffectAnimation())
				saveOrEditAddress()
			}

			edtAddressDescription.setHorizontallyScrolling(false)
			edtAddressDescription.maxLines = 3


			initTxtAboveEdtsDuringFocus(
				edtAddressName,
				txtAddressName,
				line2,
				curActivity().getStringResource(R.string.address_book_add_contact_name)
			)

			initTxtAboveEdtsDuringFocus(
				edtAddressWallet,
				txtAddressWallet,
				line3,
				curActivity().getStringResource(R.string.address_book_add_contact_adress)
			)

			initTxtAboveEdtsDuringFocus(
				edtAddressDescription,
				txtAddressDescription,
				line5,
				curActivity().getStringResource(R.string.address_book_edit_contact_description)
			)


		}


	}


	private val requestPermissions =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { it ->
			it.entries.forEach {
				if (it.value) {
					curActivity().mainViewModel.saveDecodeQrCode("")
					curActivity().move2ScannerFragment(null, null)
				} else
					Toast.makeText(
						curActivity(),
						curActivity().getStringResource(R.string.camera_permission_missing),
						Toast.LENGTH_SHORT
					)
						.show()
			}
		}

	private var jobDecodedAddress: Job? = null

	private fun getQrCodeDecoded() {
		jobDecodedAddress?.cancel()
		jobDecodedAddress=lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				curActivity().mainViewModel.decodedQrCode.collect { decodeQr ->
					if (decodeQr.isNotEmpty()) {
						binding.apply {
							edtAddressWallet.setText(decodeQr)
							txtAddressWallet.visibility = View.VISIBLE
						}
						curActivity().mainViewModel.saveDecodeQrCode("")
					}
				}
			}
		}
	}


	private fun initTxtAboveEdtsDuringFocus(
		edt: EditText,
		txt: TextView,
		line: View,
		hint: String
	) {
		edt.setOnFocusChangeListener { view, focus ->
			if (focus) {
				txt.visibility = View.VISIBLE
				edt.hint = ""
				line.setBackgroundColor(curActivity().getColorResource(R.color.green))
			} else if (edt.text.toString().isEmpty()) {
				txt.visibility = View.INVISIBLE
				edt.hint = hint
			}
			if (!focus) {
				line.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
			}
		}
	}

	private fun saveOrEditAddress() {
		binding.apply {
			if (addressItem != null) {
				//updating
				dialogManager.showSuccessDialog(
					curActivity(),
					curActivity().getStringResource(R.string.adress_book_edit_contact_pop_up_changed_title),
					curActivity().getStringResource(R.string.adress_book_edit_contact_pop_up_changed_description),
					curActivity().getString(R.string.ready_btn)
				) {
					curActivity().move2AddressFragmentList()
				}
				val updatingAddress = getAddress()
				addressViewModel.updateAddressItem(updatingAddress)
			} else {
				//saving new one
				saveAddressJob()
			}
		}
	}

	private var saveAddressJob: Job? = null

	private fun saveAddressJob() {
		saveAddressJob?.cancel()
		saveAddressJob = lifecycleScope.launch {
			val newAddress = getAddress()
			val addressExist = addressViewModel.checkIfAddressExistInDb(newAddress.address)
			if (addressExist.isNotEmpty()) {
				curActivity().apply {
					dialogManager.showFailureDialog(
						this,
						getStringResource(R.string.pop_up_failed_error_title),
						getStringResource(R.string.pop_up_failed_error_description_address_already_exists),
						getStringResource(R.string.return_btn)
					) {

					}
				}
			} else {
				dialogManager.showSuccessDialog(
					curActivity(),
					curActivity().getStringResource(R.string.address_book_pop_up_added_title),
					curActivity().getStringResource(R.string.address_book_pop_up_added_description),
					curActivity().getString(R.string.ready_btn)
				) {
					curActivity().move2AddressFragmentList()
				}
				VLog.d("Saving new AddressItem : ${newAddress}")
				addressViewModel.insertAddressItem(newAddress)
			}
		}
	}

	private fun getAddress(): Address {
		val address_id =
			if (addressItem == null) UUID.randomUUID().toString() else addressItem!!.address_id
		binding.apply {
			return Address(
				address_id,
				address = edtAddressWallet.text.toString().trim(),
				name = edtAddressName.text.toString().trim(),
				description = edtAddressDescription.text.toString().trim(),
				System.currentTimeMillis()
			)
		}
	}

	private fun curActivity() = requireActivity() as MainActivity


}
