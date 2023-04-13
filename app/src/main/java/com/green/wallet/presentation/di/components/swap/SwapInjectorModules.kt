package com.green.wallet.presentation.di.components.swap

import androidx.navigation.NavController
import com.green.wallet.presentation.main.swap.exchange.ExchangeFragment
import com.green.wallet.presentation.main.swap.request.RequestFragment
import com.green.wallet.presentation.onboard.OnBoardActivity
import dagger.BindsInstance
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class SwapInjectorModules {

	@ContributesAndroidInjector
	abstract fun injectExchangeFragment(): ExchangeFragment

	@ContributesAndroidInjector
	abstract fun injectRequestFragment(): RequestFragment

}
