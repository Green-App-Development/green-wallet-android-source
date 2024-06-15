package com.green.wallet.data.network.firebase

import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.green.wallet.data.network.dto.firebase.DAppLinkFirebase
import com.green.wallet.domain.domainmodel.DAppLink
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await


class FirebaseDBImpl : FirebaseDB {

    private val rootDAppList = Firebase.database.getReference("dAppList").child("dApps")
    override suspend fun getDAppLinkFirebaseList(): List<DAppLink> {
        val result = mutableListOf<DAppLink>()
        rootDAppList.get().await().children.forEach { it ->
            val map = rootDAppList.child(it.key!!).get().await().value as HashMap<*, *>
            val name = map["Name"] as String
            val category = (map["Category"] as String).split(",").map { it.trim() }
            val icon = map["Icon"] as String
            val link = map["Link"] as String
            val verified = (map["Verified"] as? Boolean) ?: false
            val description = map["Description"] as String

            val dAppLink = DAppLink(name, description, link, icon, false, category, verified)
            result.add(dAppLink)
        }

        return result
    }

}