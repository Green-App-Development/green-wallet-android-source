package com.android.greenapp.presentation.main.noconnection

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentNoInternetBinding
import android.content.Intent
import android.provider.Settings
import com.example.common.tools.VLog


class NoConnectionFragment : Fragment(R.layout.fragment_no_internet) {

	private lateinit var binding: FragmentNoInternetBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentNoInternetBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.btnReConnectBtn.setOnClickListener {
			kotlin.runCatching {
				val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
				startActivity(intent)
			}.onFailure {
				VLog.d("On failure calling network settings menu : $it  ")
			}

		}

	}


}
