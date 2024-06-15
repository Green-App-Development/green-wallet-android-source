package com.green.wallet.domain.interact

import com.green.wallet.data.network.dto.firebase.DAppLinkFirebase
import com.green.wallet.domain.domainmodel.DAppLink

interface FirebaseInteract {

    suspend fun getListOfDAppLink(): List<DAppLink>



}