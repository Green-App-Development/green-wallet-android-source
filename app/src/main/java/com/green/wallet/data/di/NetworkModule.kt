package com.green.wallet.data.di

import androidx.navigation.Navigator
import com.google.gson.GsonBuilder
import com.green.wallet.BuildConfig
import com.green.wallet.data.network.DexieService
import com.green.wallet.data.network.ExchangeService
import com.green.wallet.data.network.GreenAppService
import com.green.wallet.data.network.TibetExchangeService
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
			level = HttpLoggingInterceptor.Level.BODY
		}

		return Retrofit.Builder().baseUrl(BuildConfig.BASE_URL_GREEN_APP)
			.client(getUnsafeOkHttpClient(interceptor))
			.addConverterFactory(
				GsonConverterFactory.create()
			).build()
	}


	@AppScope
	@Provides
	@Named("retrofit_tibet_api")
	fun provideDexieRetrofitInstance(): Retrofit {

		val interceptor = HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.BODY
		}

		return Retrofit.Builder().baseUrl(BuildConfig.TIBET_API)
			.client(getUnsafeOkHttpClient(interceptor))
			.addConverterFactory(
				GsonConverterFactory.create()
			).build()
	}

	@AppScope
	@Provides
	@Named("retrofit_dexie_api")
	fun provideTibetRetrofitInstance(): Retrofit {

		val interceptor = HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.BODY
		}

		return Retrofit.Builder().baseUrl(BuildConfig.DEXIE_API)
			.client(getUnsafeOkHttpClient(interceptor))
			.addConverterFactory(
				GsonConverterFactory.create()
			).build()
	}


	@Provides
	@AppScope
	fun provideLanguageService(@Named("retrofit_green_app") retrofit: Retrofit) = retrofit.create(
		GreenAppService::class.java
	)

	@Provides
	@AppScope
	fun provideExchangeService(@Named("retrofit_green_app") retrofit: Retrofit) = retrofit.create(
		ExchangeService::class.java
	)

	@Provides
	fun provideTibetExchangeService(@Named("retrofit_tibet_api") retrofit: Retrofit) =
		retrofit.create(
			TibetExchangeService::class.java
		)


	@Provides
	fun provideDexieExchangeService(@Named("retrofit_dexie_api") retrofit: Retrofit) =
		retrofit.create(
			DexieService::class.java
		)

	@Provides
	@AppScope
	fun provideRetrofitBuilder(): Retrofit.Builder {
		val interceptor = HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.BODY
		}
		return Retrofit.Builder()
			.client(getUnsafeOkHttpClient(interceptor))
			.addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
	}


}
