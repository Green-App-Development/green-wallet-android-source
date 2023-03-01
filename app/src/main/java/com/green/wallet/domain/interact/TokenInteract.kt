package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.Token
import com.green.wallet.presentation.tools.Resource


interface TokenInteract {


    suspend fun getTokenListAndSearchForWallet(fingerPrint:Long, nameCode:String?): Resource<List<Token>>

    suspend fun getTokenPriceByCode(code:String):Double

    suspend fun getTokenListDefaultOnMainScreen():List<Token>
}
