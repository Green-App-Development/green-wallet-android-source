package com.android.greenapp.domain.interact

import com.android.greenapp.domain.domainmodel.Token

/**
 * Created by bekjan on 12.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface TokenInteract {


    suspend fun getTokenListAndSearchForWallet(fingerPrint:Long, nameCode:String?): List<Token>

    suspend fun getTokenPriceByCode(code:String):Double

    suspend fun getTokenListDefaultOnMainScreen():List<Token>
}
