package com.green.wallet.presentation.di.application

import android.content.Context
import com.green.wallet.data.di.InteractModule
import com.green.wallet.data.di.NetworkModule
import com.green.wallet.presentation.App
import com.green.wallet.presentation.main.btmdialogs.BtmChooseWallet
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector


@AppScope
@Component(modules = [AndroidInjectionModule::class, InteractModule::class, InjectorBuildersModule::class, ViewModelsModule::class, AppModule::class, NetworkModule::class, WorkerBindingModule::class])
interface AppComponent : AndroidInjector<App> {

	fun fcmComponentBuilder(): FCMServiceComponent.Builder
	fun inject(dialogWallet: BtmChooseWallet)

	@Component.Builder
	interface Builder {

		@BindsInstance
		fun bindApplication(context: Context): Builder
		fun build(): AppComponent

	}


}
