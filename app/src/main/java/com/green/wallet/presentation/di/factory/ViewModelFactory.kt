package com.green.wallet.presentation.di.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

/**
 * Created by bekjan on 30.11.2021.
 * email: bekjan.omirzak98@gmail.com
 */

class ViewModelFactory @Inject constructor(private val viewModels: MutableMap<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) = viewModels[modelClass]?.get() as T

}
