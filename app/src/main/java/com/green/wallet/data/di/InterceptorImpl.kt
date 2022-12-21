package com.green.wallet.data.di

import com.green.wallet.presentation.di.application.AppScope
import com.green.wallet.presentation.tools.BASE_URL_BLOCKCHAIN
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request


/**
 * Created by bekjan on 04.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */

@AppScope
class InterceptorImpl @Inject constructor() : Interceptor {

    @Volatile
    private var host: String? = BASE_URL_BLOCKCHAIN


    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        val host = host
        if (host != null) {
            //HttpUrl newUrl = request.url().newBuilder()
            //    .host(host)
            //    .build();
            val newUrl = host.toHttpUrlOrNull()
            request = request.newBuilder()
                .url(newUrl!!)
                .build()
        }
        return chain.proceed(request)
    }


}
