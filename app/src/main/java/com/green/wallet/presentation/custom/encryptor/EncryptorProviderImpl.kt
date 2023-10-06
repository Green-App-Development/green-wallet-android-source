package com.green.wallet.presentation.custom.encryptor

import android.content.Context

class EncryptorProviderImpl(private val context: Context) : EncryptorProvider {

    lateinit var encryptor: Encryptor

    override fun setStage(stage: Int) {
        encryptor = when (stage) {
            FIRST_STAGE -> FirstStageEncryptor()
            SECOND_STAGE -> SecondStageEncryptor(context)
            else -> SecondStageEncryptor(context)
        }
    }

    override fun encrypt(text: String, alias: String): String {
        return encryptor.encrypt(text, alias)
    }

    override fun decrypt(text: String, alias: String): String {
        return encryptor.decrypt(text, alias)
    }


    companion object {
        const val FIRST_STAGE = 1
        const val SECOND_STAGE = 2
    }

}