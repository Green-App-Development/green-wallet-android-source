package com.android.greenapp.presentation.di.application

import android.app.Application
import android.content.Context
import com.android.greenapp.data.di.InteractModule
import com.android.greenapp.data.di.NetworkModule
import com.android.greenapp.presentation.App
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector


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