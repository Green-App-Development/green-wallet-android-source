package com.green.wallet.presentation.custom.encryptor

interface Encryptor {

    fun decrypt(strToDecrypt: String, alias: String): String

    fun encrypt(strToEncrypt: String, alias: String): String

}
