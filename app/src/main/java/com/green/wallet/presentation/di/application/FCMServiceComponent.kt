package com.green.wallet.presentation.di.application

import com.green.wallet.data.network.FirebasePushNotifService
import dagger.android.AndroidInjector

/**
 * Created by bekjan on 23.02.2023.
 * email: bekjan.omirzak98@gmail.com
 */
@dagger.Subcomponent
interface FCMServiceComponent : AndroidInjector<FirebasePushNotifService> {

	@dagger.Subcomponent.Builder
	interface Builder {
		fun build(): FCMServiceComponent
	}

}
