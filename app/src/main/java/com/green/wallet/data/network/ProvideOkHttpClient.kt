package com.green.wallet.data.network

import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.lang.Exception
import java.lang.RuntimeException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun getUnsafeOkHttpClient(interceptor: Interceptor): OkHttpClient {
    try {
        val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {

            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

        })

        // install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        // create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        val timeoutInSec = 20
        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 5

        val builder = OkHttpClient.Builder()
            .connectTimeout(timeoutInSec.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutInSec.toLong(), TimeUnit.SECONDS)
            .dispatcher(dispatcher)
            .addInterceptor(interceptor)

        builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)

        val hostnameVerifier = HostnameVerifier { hostname, session ->
            true
        }

        builder.hostnameVerifier(hostnameVerifier)
        return builder.build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}
