package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.DexieFees

interface DexieInteract {

    suspend fun getDexieMinFee(): DexieFees

}