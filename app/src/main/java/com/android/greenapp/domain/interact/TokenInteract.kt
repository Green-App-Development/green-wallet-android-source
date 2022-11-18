package com.android.greenapp.domain.interact

import com.android.greenapp.domain.domainmodel.Token
import com.android.greenapp.presentation.tools.Resource

/**
 * Created by bekjan on 12.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface TokenInteract {


    suspend fun getTokenListAndSearchForWallet(fingerPrint:Long, nameCode:String?): Resource<List<Token>>

    suspend fun getTokenPriceByCode(code:String):Double

    suspend fun getTokenListDefaultOnMainScreen():List<Token>
}
