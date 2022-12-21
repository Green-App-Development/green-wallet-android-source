package com.green.wallet.presentation.di.application

import android.content.Context
import com.green.wallet.data.di.NetworkModule
import com.green.wallet.presentation.App
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import com.green.wallet.data.di.InteractModule


@AppScope
@Component(modules = [AndroidInjectionModule::class, InteractModule::class, InjectorBuildersModule::class, ViewModelsModule::class, AppModule::class, NetworkModule::class, WorkerBindingModule::class])
interface AppComponent : AndroidInjector<App> {


    @Component.Builder
    interface Builder {

        @BindsInstance
        fun bindApplication(context: Context): Builder
        fun build(): AppComponent

    }


}
