package com.green.wallet.domain.usecases.tibet

import android.content.Context
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.convertListToStringWithSpace
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckingCATOnHome @Inject constructor(
	private val walletInteract: WalletInteract,
	private val context: Context
) {

	suspend operator fun invoke(address: String, assetId: String) {
		val catImported = walletInteract.checkingTokenOnHome(address, assetId)
		if (catImported || address.isEmpty())
			return
		val methodChannel = MethodChannel(
			(context.applicationContext as App).flutterEngine.dartExecutor.binaryMessenger,
			METHOD_CHANNEL_GENERATE_HASH
		)
		val mainPuzzleHashes = walletInteract.getMainPuzzleHashes(address)
		val map = hashMapOf<String, String>()
		map["puzzle_hashes"] = convertListToStringWithSpace(mainPuzzleHashes)
		map["asset_id"] = assetId
		methodChannel.setMethodCallHandler { method, calLBack ->
			if (method.method == "generate_outer_hash") {
				val args = method.arguments as HashMap<*, *>
				val outer_hashes = args[assetId]!! as List<String>
				CoroutineScope(Dispatchers.IO).launch {
					walletInteract.importTokenByAddress(address, true, assetId, outer_hashes)
				}
			}
		}
		withContext(Dispatchers.Main) {
			methodChannel.invokeMethod("generatewrappedcatpuzzle", map)
		}
	}


}
