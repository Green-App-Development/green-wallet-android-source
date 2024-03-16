package com.green.wallet.data.di

import com.green.wallet.data.interact.AddressInteractImpl
import com.green.wallet.data.interact.BlockChainInteractImpl
import com.green.wallet.data.interact.CancelTransactionInteractImpl
import com.green.wallet.data.interact.CryptocurrencyImpl
import com.green.wallet.data.interact.DAppOfferInteractImpl
import com.green.wallet.data.interact.DexieInteractImpl
import com.green.wallet.data.interact.ExchangeInteractImpl
import com.green.wallet.data.interact.FirebaseInteractImpl
import com.green.wallet.data.interact.GreenAppInteractImpl
import com.green.wallet.data.interact.NFtInteractImpl
import com.green.wallet.data.interact.NotifinteractImpl
import com.green.wallet.data.interact.OfferTransactionInteractImpl
import com.green.wallet.data.interact.SearchInteractImpl
import com.green.wallet.data.interact.SpentCoinsInteractImpl
import com.green.wallet.data.interact.SupportInteractImpl
import com.green.wallet.data.interact.TibetInteractImpl
import com.green.wallet.data.interact.TokenInteractImpl
import com.green.wallet.data.interact.TransactionInteractImpl
import com.green.wallet.data.interact.WalletInteractImpl
import com.green.wallet.data.network.firebase.FirebaseDB
import com.green.wallet.data.network.firebase.FirebaseDBImpl
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.AddressInteract
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.CancelTransactionInteract
import com.green.wallet.domain.interact.CryptocurrencyInteract
import com.green.wallet.domain.interact.DAppOfferInteract
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.FirebaseInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.NotifInteract
import com.green.wallet.domain.interact.OfferTransactionInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.SearchInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.SupportInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.TransactionInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.di.application.AppScope
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module
abstract class InteractModule {

    @Binds
    abstract fun bindsPrefsInteractImpl(prefsManager: PrefsManager): PrefsInteract

    @Binds
    abstract fun bindContactInteractImpl(addressInteract: AddressInteractImpl): AddressInteract

    @Binds
    @AppScope
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
    abstract fun bindSupportInteractImpl(supportInteractImpl: SupportInteractImpl): SupportInteract

    @Binds
    abstract fun bindTokenInteractImpl(tokenInteractImpl: TokenInteractImpl): TokenInteract

    @Binds
    abstract fun bindSpentCoinsInteractImpl(spentCoinsInteract: SpentCoinsInteractImpl): SpentCoinsInteract

    @Binds
    abstract fun bindNFTInteractImpl(nftInteract: NFtInteractImpl): NFTInteract

    @Binds
    abstract fun bindExchangeInteractImpl(exchangeInteract: ExchangeInteractImpl): ExchangeInteract

    @Binds
    abstract fun bindTibetInteractImpl(tibetInteractImpl: TibetInteractImpl): TibetInteract

    @Binds
    abstract fun bindDexieInteractImpl(dexieInteractImpl: DexieInteractImpl): DexieInteract

    @Binds
    abstract fun bindDAppInteractImpl(dAppOfferInteract: DAppOfferInteractImpl): DAppOfferInteract

    @Binds
    abstract fun bindOfferTransactionImpl(offerTranImpl: OfferTransactionInteractImpl): OfferTransactionInteract

    @Binds
    abstract fun bindCancelTransactionImpl(cancelTransactionInteractImpl: CancelTransactionInteractImpl): CancelTransactionInteract

    @Binds
    abstract fun bindFirebaseInteractImpl(firebaseInteractImpl: FirebaseInteractImpl): FirebaseInteract

    @Binds
    abstract fun bindSearchInteractImpl(searchInteract: SearchInteractImpl): SearchInteract

}
