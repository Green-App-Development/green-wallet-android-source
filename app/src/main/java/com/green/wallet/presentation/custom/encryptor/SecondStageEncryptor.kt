package com.green.wallet.presentation.custom.encryptor

import android.content.Context

class SecondStageEncryptor(val context: Context) : Encryptor {
    override fun decrypt(strToDecrypt: String, alias: String) =
        EncryptionUtils.decrypt(context, strToDecrypt, alias) ?: ""

    override fun encrypt(strToEncrypt: String, alias: String) =
        EncryptionUtils.encrypt(context, strToEncrypt, alias) ?: ""

}