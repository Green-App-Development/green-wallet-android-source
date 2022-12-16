package extralogic.wallet.greenapp.presentation.di.application

import android.content.Context
import extralogic.wallet.greenapp.data.di.NetworkModule
import extralogic.wallet.greenapp.presentation.App
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import extralogic.wallet.greenapp.data.di.InteractModule


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