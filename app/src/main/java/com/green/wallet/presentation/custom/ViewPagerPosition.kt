package com.green.wallet.presentation.custom

import com.green.wallet.presentation.di.application.AppScope
import javax.inject.Inject


@AppScope
class ViewPagerPosition @Inject constructor() {

	var pagerPosition: Int = 0

}
