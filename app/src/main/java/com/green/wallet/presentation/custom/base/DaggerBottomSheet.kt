package com.green.wallet.presentation.custom.base

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


abstract class DaggerBottomSheet<V : ViewModel?> : BottomSheetDialogFragment(), HasAndroidInjector {


    protected var mViewModel: V? = null
    abstract fun getViewModel(): V

    @Inject
    var androidInjector: DispatchingAndroidInjector<Any>? = null
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector!!
    }

}



