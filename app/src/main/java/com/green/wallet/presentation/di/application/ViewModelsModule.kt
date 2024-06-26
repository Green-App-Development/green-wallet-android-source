package com.green.wallet.presentation.di.application

import androidx.lifecycle.ViewModel
import com.green.wallet.presentation.di.factory.ViewModelKey
import com.green.wallet.presentation.onboard.OnBoardViewModel
import com.green.wallet.presentation.intro.IntroActViewModel
import com.green.wallet.presentation.main.MainViewModel
import com.green.wallet.presentation.main.address.AddressViewModel
import com.green.wallet.presentation.main.allsettings.AllSettingsViewModel
import com.green.wallet.presentation.main.createnewwallet.NewWalletViewModel
import com.green.wallet.presentation.main.dapp.DAppViewModel
import com.green.wallet.presentation.main.dapp.browser.BrowserViewModel
import com.green.wallet.presentation.main.dapp.trade.TraderViewModel
import com.green.wallet.presentation.main.enterpasscode.EnterPasscodeViewModel
import com.green.wallet.presentation.main.home.HomeFragmentViewModel
import com.green.wallet.presentation.main.impmnemonics.ImpMnemonicViewModel
import com.green.wallet.presentation.main.importtoken.ImportTokenViewModel
import com.green.wallet.presentation.main.language.MainLanguageViewModel
import com.green.wallet.presentation.main.managewallet.ManageWalletViewModel
import com.green.wallet.presentation.main.nft.nftdetail.NFTDetailsViewModel
import com.green.wallet.presentation.main.nft.nftsend.NFTSendViewModel
import com.green.wallet.presentation.main.nft.usernfts.UserNFTTokensViewModel
import com.green.wallet.presentation.main.notification.NotifViewModel
import com.green.wallet.presentation.main.pincode.PinCodeViewModel
import com.green.wallet.presentation.main.receive.ReceiveViewModel
import com.green.wallet.presentation.main.send.SendViewModel
import com.green.wallet.presentation.main.support.SupportViewModel
import com.green.wallet.presentation.main.swap.exchange.ExchangeViewModel
import com.green.wallet.presentation.main.swap.main.SwapMainViewModel
import com.green.wallet.presentation.main.swap.order.OrdersViewModel
import com.green.wallet.presentation.main.swap.requestdetail.OrderDetailViewModel
import com.green.wallet.presentation.main.swap.send.SwapSendViewModel
import com.green.wallet.presentation.main.swap.tibetliquiditydetail.TibetLiquidityDetailViewModel
import com.green.wallet.presentation.main.swap.tibetswap.TibetSwapViewModel
import com.green.wallet.presentation.main.swap.tibetswapdetail.TibetSwapDetailViewModel
import com.green.wallet.presentation.main.transaction.TransactionsViewModel
import com.green.wallet.presentation.main.transaction.btmCancel.CancelOfferViewModel
import com.green.wallet.presentation.main.transaction.btmSpeedy.SpeedyDialogViewModel
import com.green.wallet.presentation.main.walletlist.WalletListViewModel
import com.green.wallet.presentation.main.walletsettings.WalletSettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
abstract class ViewModelsModule {

    @IntoMap
    @Binds
    @ViewModelKey(OnBoardViewModel::class)
    abstract fun bindsGreetingFragmentViewModel(greetingFragmentViewModel: OnBoardViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(ImpMnemonicViewModel::class)
    abstract fun bindsImpMnemonicViewModel(mneMonicViewModel: ImpMnemonicViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(MainViewModel::class)
    abstract fun bindsMainViewModel(mainViewModel: MainViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(SendViewModel::class)
    abstract fun bindsSendFragmentViewModel(sendFragmentViewModel: SendViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(IntroActViewModel::class)
    abstract fun bindsIntroActViewModel(introActViewModel: IntroActViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(AllSettingsViewModel::class)
    abstract fun bindsAllSettingsViewModel(settings: AllSettingsViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(HomeFragmentViewModel::class)
    abstract fun bindsHomeFragmentViewModel(homeFragmentViewModel: HomeFragmentViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(MainLanguageViewModel::class)
    abstract fun bindMainLanguageViewModel(mainLanguageViewModel: MainLanguageViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SupportViewModel::class)
    abstract fun bindSupportViewModel(supportViewModel: SupportViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(AddressViewModel::class)
    abstract fun bindingAddressViewModel(addressViewModel: AddressViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(NewWalletViewModel::class)
    abstract fun bindingCreateNewWalletViewModel(newWalletViewModel: NewWalletViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(WalletListViewModel::class)
    abstract fun bindingWalletListViewModel(walletListViewModel: WalletListViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(ManageWalletViewModel::class)
    abstract fun bindingManageWalletViewModel(manageWalletViewModel: ManageWalletViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(ReceiveViewModel::class)
    abstract fun bindingReceiveViewModel(receiveViewModel: ReceiveViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(TransactionsViewModel::class)
    abstract fun bindingTransactionViewModel(transViewModel: TransactionsViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(NotifViewModel::class)
    abstract fun bindingNotificationViewModel(notifViewModel: NotifViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(EnterPasscodeViewModel::class)
    abstract fun bindingPasscodeViewModel(enterPasscodeViewModel: EnterPasscodeViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(ImportTokenViewModel::class)
    abstract fun bindingImpTokenViewModel(importTokenViewModel: ImportTokenViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(WalletSettingsViewModel::class)
    abstract fun bindingWalletSettingsViewModel(settingsViewModel: WalletSettingsViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(UserNFTTokensViewModel::class)
    abstract fun bindingUserNFtTokensViewModel(userNftViewModel: UserNFTTokensViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(NFTDetailsViewModel::class)
    abstract fun bindingNFtDetailsViewModel(nftViewModel: NFTDetailsViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(DAppViewModel::class)
    abstract fun bindingDAppViewModel(viewModel: DAppViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SwapMainViewModel::class)
    @AppScope
    abstract fun bindingSwapViewModel(viewModel: SwapMainViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(NFTSendViewModel::class)
    abstract fun bindingNFTSendViewModel(viewModel: NFTSendViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(ExchangeViewModel::class)
    abstract fun bindingExchangeViewModel(viewModel: ExchangeViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(OrderDetailViewModel::class)
    abstract fun bindingOrderDetailViewModel(viewModel: OrderDetailViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(OrdersViewModel::class)
    abstract fun bindingOrdersViewModel(viewModel: OrdersViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SwapSendViewModel::class)
    abstract fun bindingSwapSendViewModel(viewModel: SwapSendViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(TibetSwapViewModel::class)
    @AppScope
    abstract fun bindingTibetSwapViewModel(viewModel: TibetSwapViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(TibetSwapDetailViewModel::class)
    abstract fun bindingTibetSwapDetailViewModel(viewModel: TibetSwapDetailViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(TibetLiquidityDetailViewModel::class)
    abstract fun bindingTibetLiquidityDetailViewModel(viewModel: TibetLiquidityDetailViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(TraderViewModel::class)
    abstract fun bindingTraderViewModel(viewModel: TraderViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(SpeedyDialogViewModel::class)
    abstract fun bindingCatDialogViewModel(viewModel: SpeedyDialogViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(PinCodeViewModel::class)
    abstract fun bindingPinCodeViewModel(viewModel: PinCodeViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(CancelOfferViewModel::class)
    abstract fun bindingCancelOfferViewModel(viewModel: CancelOfferViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(BrowserViewModel::class)
    abstract fun bindingBrowserViewModel(viewModel: BrowserViewModel): ViewModel

}
