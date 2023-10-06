package com.green.wallet.presentation.custom.encryptor

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class FirstStageEncryptor : Encryptor {

    override fun decrypt(strToDecrypt: String, alias: String): String {
        try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, getAESKey(alias))
            return String(
                cipher.doFinal(
                    android.util.Base64.decode(
                        strToDecrypt,
                        android.util.Base64.DEFAULT
                    )
                )
            )
        } catch (ex: Exception) {
            println("Exception in decrypting using AES  : ${ex.message}")
        }
        return ""
    }

    override fun encrypt(strToEncrypt: String, alias: String): String {
        try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, getAESKey(alias))
            val final = cipher.doFinal(strToEncrypt.toByteArray(charset("UTF-8")))
            return android.util.Base64.encodeToString(final, android.util.Base64.DEFAULT)
        } catch (ex: Exception) {
            println("Exception in encrypting using AES  : ${ex.message}")
        }
        return ""
    }

    private fun getAESKey(myKey: String): SecretKeySpec? {
        val sha: MessageDigest?
        try {
            var key = myKey.toByteArray(charset("UTF-8"))
            sha = MessageDigest.getInstance("SHA-1")
            key = sha.digest(key)
            key = key.copyOf(16)
            return SecretKeySpec(key, "AES")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return null
    }


}

