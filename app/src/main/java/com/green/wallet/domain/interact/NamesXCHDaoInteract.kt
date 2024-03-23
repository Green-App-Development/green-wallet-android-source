package com.green.wallet.domain.interact

interface NamesXCHDaoInteract {

    suspend fun getNamesXCHAddress(
        addressName: String
    ): String

}