package com.green.wallet.presentation.di.components.swap

import androidx.navigation.NavController
import com.green.wallet.presentation.main.swap.exchange.ExchangeFragment
import dagger.BindsInstance


@dagger.Subcomponent(modules = [SwapModule::class])
@SwapScope
interface SwapComponent {

	fun inject(exchangeFragment: ExchangeFragment)

	@dagger.Subcomponent.Builder
	interface Builder {
		@BindsInstance
		fun bindNavController(navController: NavController): Builder
		fun build(): SwapComponent
	}

}
