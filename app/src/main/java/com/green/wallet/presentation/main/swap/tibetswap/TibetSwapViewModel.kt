package com.green.wallet.presentation.main.swap.tibetswap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Token
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TibetSwapViewModel @Inject constructor(
	private val tokenInteract: TokenInteract
) : ViewModel() {

	var isShowingSwap = true

	var xchToCAT = true
	var nextHeight = 435
	val containerBiggerSize = 435
	val containerSmallerSize = 265
	var nextContainerBigger = true

	private val _tokenList = MutableStateFlow<List<Token>>(emptyList())
	val tokenList = _tokenList.asStateFlow()

	init {
		VLog.d("On create tibet vm : $this")
		retrieveTokenList()
	}

	private fun retrieveTokenList() {
		viewModelScope.launch {
			val res = tokenInteract.getTokenListPairIDExist()
			_tokenList.emit(res)
		}
	}


	override fun onCleared() {
		super.onCleared()
		VLog.d("On cleared tibet vm : $this")
	}

}
