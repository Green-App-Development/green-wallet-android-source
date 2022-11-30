package com.android.greenapp.presentation.main.walletsettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.greenapp.databinding.FragmentWalletSettingsBinding
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.preventDoubleClick
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Created by bekjan on 29.11.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class WalletSettingsFragment : DaggerFragment() {


	private lateinit var binding: FragmentWalletSettingsBinding

	@Inject
	lateinit var dialog: DialogManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentWalletSettingsBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.registerClicks()
	}


	private fun FragmentWalletSettingsBinding.registerClicks() {

		btnSaveSettings.setOnClickListener {
			it.preventDoubleClick()
			dialog.showSuccessDialog(
				curActivity(),
				"Saved",
				"Wallet settings saved successfully!",
				"Done",
				isDialogOutsideTouchable = false
			) {

			}
		}

		txtDefaultSettings.setOnClickListener {
			dialog.showAssuranceDialogDefaultSetting(
				curActivity(),
				"Restore default settings?",
				"This action will return the settings of the wallet derivative index to the default values, which may lead to incorrect display of the balance",
				btnYes = {

				},
				btnNo = {

				}
			)
		}


	}


	private fun curActivity() = requireActivity() as MainActivity


}
