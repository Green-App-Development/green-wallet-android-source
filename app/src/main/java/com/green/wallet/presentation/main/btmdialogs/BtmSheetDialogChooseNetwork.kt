package com.green.wallet.presentation.main.btmdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.green.wallet.R
import com.green.wallet.presentation.custom.dialogs.NetworkAdapter
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.getStringResource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.ArrayList

class BtmSheetDialogChooseNetwork :
	BottomSheetDialogFragment() {

	lateinit var dataList: ArrayList<String>
	var hasAtLeastOneWallet: Boolean = false

	companion object {
		var DATA_LIST_KEY = "data_list_key"
		var HAS_AT_LEAST_ONE = "has_at_least_one"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			dataList = it.getStringArrayList(DATA_LIST_KEY)!!
			hasAtLeastOneWallet = it.getBoolean(HAS_AT_LEAST_ONE)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.dialog_choose_network, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val networkRecView = view.findViewById<RecyclerView>(R.id.rec_view_network)
		val networkAdapter =
			NetworkAdapter(curActivity(), object : NetworkAdapter.ChooseNetworkListener {
				override fun onNetworkClicked(position: Int) {
					curActivity().curChosenNetworkTypePosForBackImport = dataList[position]
					curActivity().showBtmDialogAddOrImportWallets(dataList[position])
				}
			})
		networkRecView?.adapter = networkAdapter
		networkAdapter.updateNetworkList(dataList)
		view.findViewById<TextView>(R.id.txtSelectNetwork)?.setText(
			curActivity().getStringResource(if (hasAtLeastOneWallet) R.string.select_network else R.string.select_network_new_wallet)
		)
	}

	override fun getTheme(): Int {
		return R.style.AppBottomSheetDialogTheme
	}

	private fun curActivity() = requireActivity() as MainActivity

}
