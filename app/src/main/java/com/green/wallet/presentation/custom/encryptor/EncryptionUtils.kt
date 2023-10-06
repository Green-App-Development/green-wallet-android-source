package com.green.wallet.presentation.custom.encryptor

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import timber.log.Timber
import java.security.KeyStore
import java.security.KeyStoreException

object EncryptionUtils {

    private val keyStore: KeyStore?
        get() = try {
            val keyStore = KeyStore.getInstance(EncryptionKeyGenerator.ANDROID_KEY_STORE)
            keyStore?.load(null)
            keyStore
        } catch (e: Exception) {
            Timber.e(e)
            null
        }

    fun encrypt(context: Context, token: String, alias: String): String? {
        val securityKey = getSecurityKey(context, alias)
        return securityKey?.encrypt(token)
    }

    fun decrypt(context: Context, token: String, alias: String): String? {
        val securityKey = getSecurityKey(context, alias)
        return securityKey?.decrypt(token)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun getSecurityKey(context: Context, alias: String): SecurityKey? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> keyStore?.let {
            EncryptionKeyGenerator.generateSecretKey(
                it,
                alias
            )
        }
        else -> EncryptionKeyGenerator.generateSecretKeyPre18(context, alias)
    }

    fun clear(alias: String) = try {
        val keyStore = keyStore
        if (keyStore?.containsAlias(alias) == true) {
            keyStore.deleteEntry(alias)
        } else {
            // do nothing
        }
    } catch (e: KeyStoreException) {
        Timber.e(e)
    }
}