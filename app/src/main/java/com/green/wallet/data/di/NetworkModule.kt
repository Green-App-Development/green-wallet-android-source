package com.green.wallet.data.di

import com.green.wallet.BuildConfig
import com.green.wallet.data.network.GreenAppService
import com.green.wallet.data.network.getUnsafeOkHttpClient
import com.green.wallet.presentation.di.application.AppScope
import dagger.Module
import dagger.Provides
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named


@Module
class NetworkModule {

	@AppScope
	@Provides
	@Named("retrofit_green_app")
	fun provideLanguageRetrofitInstance(): Retrofit {

		val interceptor = HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.NONE
		}
		return Retrofit.Builder().baseUrl(BuildConfig.BASE_URL_GREEN_APP)
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
