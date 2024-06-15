package com.green.wallet.data.interact

import com.green.wallet.data.network.dto.firebase.DAppLinkFirebase
import com.green.wallet.data.network.firebase.FirebaseDB
import com.green.wallet.domain.domainmodel.DAppLink
import com.green.wallet.domain.interact.FirebaseInteract
import javax.inject.Inject


class FirebaseInteractImpl @Inject constructor(
    private val firebaseDB: FirebaseDB
) : FirebaseInteract {

    override suspend fun getListOfDAppLink(): List<DAppLink> {
        return firebaseDB.getDAppLinkFirebaseList()
    }

}