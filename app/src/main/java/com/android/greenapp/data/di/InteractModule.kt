package com.android.greenapp.data.di

import com.android.greenapp.data.interact.*
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.interact.*
import dagger.Binds
import dagger.Module
import okhttp3.Interceptor


@Module
abstract class InteractModule {

    @Binds
    abstract fun bindsPrefsInteractImpl(prefsManager: PrefsManager): PrefsInteract

    @Binds
    abstract fun bindContactInteractImpl(addressInteract: AddressInteractImpl): AddressInteract

    @Binds
    abstract fun bindBlockChainInteractImpl(blockChainInteract: BlockChainInteractImpl): BlockChainInteract

    @Binds
    abstract fun bindLangInteractImpl(langInteract: GreenAppInteractImpl): GreenAppInteract

    @Binds
    abstract fun bindWalletInteractImpl(walletInteract: WalletInteractImpl): WalletInteract

    @Binds
    abstract fun bindTransactionInteractImpl(transaction: TransactionInteractImpl): TransactionInteract

    @Binds
    abstract fun bindCryptoCurrencyInteractImpl(cryptocurrencyImpl: CryptocurrencyImpl): CryptocurrencyInteract

    @Binds
    abstract fun bindNotifInteractImpl(notifinteractImpl: NotifinteractImpl): NotifInteract

    @Binds
    abstract fun bindInterceptor(interceptorImpl: InterceptorImpl): Interceptor

    @Binds
    abstract fun bindSupportInteractImpl(supportInteractImpl: SupportInteractImpl): SupportInteract

    @Binds
    abstract fun bindTokenInteractImpl(tokenInteractImpl: TokenInteractImpl): TokenInteract


}