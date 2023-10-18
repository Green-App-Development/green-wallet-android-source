package com.green.wallet.presentation.custom.encryptor

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.green.wallet.BuildConfig
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.crypto.KeyGenerator

object EncryptionKeyGenerator {

    const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_STORE_FILE_NAME = BuildConfig.KEY_STORE_FILE_NAME
    private const val KEY_STORE_PASSWORD = BuildConfig.KEY_STORE_PASSWORD

    @TargetApi(Build.VERSION_CODES.M)
    internal fun generateSecretKey(keyStore: KeyStore, alias: String): SecurityKey? {
        return try {
            if (!keyStore.containsAlias(alias)) {
                val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    ).setBlockModes(
                        KeyProperties.BLOCK_MODE_GCM
                    )
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build()
                )
                return SecurityKey(keyGenerator.generateKey())
            }

            val entry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
            SecurityKey(entry.secretKey)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }


    internal fun generateSecretKeyPre18(context: Context, alias: String): SecurityKey? {

        try {
            val androidCAStore = KeyStore.getInstance(KeyStore.getDefaultType())

            val password = KEY_STORE_PASSWORD.toCharArray()

            val isKeyStoreLoaded = loadKeyStore(context, androidCAStore, password)
            val protParam = KeyStore.PasswordProtection(password)
            if (!isKeyStoreLoaded || !androidCAStore.containsAlias(alias)) {
                //Create and save new secret key
                saveMyKeystore(context, androidCAStore, password, protParam, alias)
            }

            // Fetch Secret Key
            val pkEntry = androidCAStore.getEntry(alias, protParam) as KeyStore.SecretKeyEntry

            Timber.d("Secret Key Fetched :" + String(pkEntry.secretKey.encoded, Charsets.UTF_8))
            return SecurityKey(pkEntry.secretKey)
        } catch (e: java.lang.Exception) {
            Timber.e(e)
        }

        return null
    }

    private fun loadKeyStore(
        context: Context,
        androidCAStore: KeyStore,
        password: CharArray
    ): Boolean {
        val fis: java.io.FileInputStream
        try {
            fis = context.openFileInput(KEY_STORE_FILE_NAME)
        } catch (e: FileNotFoundException) {
            Timber.e(e)
            return false
        }

        try {
            androidCAStore.load(fis, password)
            return true
        } catch (e: java.lang.Exception) {
            Timber.e(e)
        }
        return false
    }

    @Throws(
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        IOException::class,
        CertificateException::class
    )
    private fun saveMyKeystore(
        context: Context,
        androidCAStore: KeyStore,
        password: CharArray,
        protParam: KeyStore.ProtectionParameter,
        alias: String
    ) {

        val mySecretKey = KeyGenerator.getInstance("AES").generateKey()

        val skEntry = KeyStore.SecretKeyEntry(mySecretKey)
        androidCAStore.load(null)
        androidCAStore.setEntry(alias, skEntry, protParam)
        var fos: java.io.FileOutputStream? = null
        try {
            fos = context.openFileOutput(KEY_STORE_FILE_NAME, Context.MODE_PRIVATE)

            androidCAStore.store(fos, password)
        } finally {
            fos?.close()
        }
        Timber.d("Secret Key Saved : %s", String(mySecretKey.encoded, Charsets.UTF_8))
    }
}