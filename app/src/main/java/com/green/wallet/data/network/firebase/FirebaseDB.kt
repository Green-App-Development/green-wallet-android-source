package com.green.wallet.data.network.firebase

import com.green.wallet.data.network.dto.firebase.DAppLinkFirebase
import com.green.wallet.domain.domainmodel.DAppLink

interface FirebaseDB {

    suspend fun getDAppLinkFirebaseList(): List<DAppLink>

}