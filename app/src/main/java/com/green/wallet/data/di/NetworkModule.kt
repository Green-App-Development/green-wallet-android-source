package com.green.wallet.data.di

import com.green.wallet.data.network.BlockChainService
import com.green.wallet.data.network.CryptocurrencyService
import com.green.wallet.data.network.GreenAppService
import com.green.wallet.data.network.getUnsafeOkHttpClient
import com.green.wallet.presentation.di.application.AppScope
import com.green.wallet.presentation.tools.BASE_URL_BLOCKCHAIN
import com.green.wallet.presentation.tools.BASE_URL_GREEN_APP
import dagger.Module
import dagger.Provides
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

/**
 * Created by bekjan on 25.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */

@Module
class NetworkModule {

    @AppScope
    @Provides
    @Named("retrofit_green_app")
    fun provideLanguageRetrofitInstance(): Retrofit {

        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        return Retrofit.Builder().baseUrl(BASE_URL_GREEN_APP)
            .client(getUnsafeOkHttpClient(interceptor))
            .addConverterFactory(
                GsonConverterFactory.create()
            ).build()
    }

    @Provides
    @Named("retrofit_blockchain")
    fun provideBlockchainRetrofitInstance(): Retrofit {

        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }

        return Retrofit.Builder().baseUrl(BASE_URL_BLOCKCHAIN)
            .client(getUnsafeOkHttpClient(interceptor))
            .addConverterFactory(
                GsonConverterFactory.create()
            ).build()
    }


    @Provides
    fun provideLanguageService(@Named("retrofit_green_app") retrofit: Retrofit) = retrofit.create(
        GreenAppService::class.java
    )


    @Provides
    fun provideBlockChain(@Named("retrofit_blockchain") retrofit: Retrofit) =
        retrofit.create(BlockChainService::class.java)

    @Provides
    fun provideCryptocurrencyService(@Named("retrofit_blockchain") retrofit: Retrofit) =
        retrofit.create(CryptocurrencyService::class.java)


    @Provides
    @AppScope
    fun provideRetrofitBuilder(): Retrofit.Builder {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        return Retrofit.Builder()
            .client(getUnsafeOkHttpClient(interceptor))
            .addConverterFactory(
                GsonConverterFactory.create()
            )
    }


}
