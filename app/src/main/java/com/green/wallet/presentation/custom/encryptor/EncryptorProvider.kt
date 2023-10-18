package com.green.wallet.presentation.custom.encryptor

interface EncryptorProvider {

    fun setStage(stage: Int)

    fun encrypt(text: String, alias: String):String

    fun decrypt(text: String, alias: String):String 

}