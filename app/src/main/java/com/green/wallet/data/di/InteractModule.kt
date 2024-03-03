package com.green.wallet.data.di

import dagger.Binds
import dagger.Module
import com.green.wallet.data.interact.*
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.*
import com.green.wallet.presentation.di.application.AppScope


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


}
