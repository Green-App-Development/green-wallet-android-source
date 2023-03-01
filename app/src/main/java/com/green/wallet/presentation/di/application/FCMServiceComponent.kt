package com.green.wallet.presentation.di.application

import com.green.wallet.data.network.FirebasePushNotifService
import dagger.android.AndroidInjector


@dagger.Subcomponent
interface FCMServiceComponent : AndroidInjector<FirebasePushNotifService> {

	@dagger.Subcomponent.Builder
	interface Builder {
		fun build(): FCMServiceComponent
	}

}
