package com.android.greenapp.presentation.di.application

import androidx.lifecycle.ViewModel
import com.android.greenapp.presentation.di.factory.ViewModelKey
import com.android.greenapp.presentation.onboard.OnBoardViewModel
import com.android.greenapp.presentation.intro.IntroActViewModel
import com.android.greenapp.presentation.main.MainViewModel
import com.android.greenapp.presentation.main.address.AddressViewModel
import com.android.greenapp.presentation.main.allsettings.AllSettingsViewModel
import com.android.greenapp.presentation.main.createnewwallet.NewWalletViewModel
import com.android.greenapp.presentation.main.enterpasscode.EnterPasscodeViewModel
import com.android.greenapp.presentation.main.home.HomeFragmentViewModel
import com.android.greenapp.presentation.main.impmnemonics.ImpMnemonicViewModel
import com.android.greenapp.presentation.main.importtoken.ImportTokenViewModel
import com.android.greenapp.presentation.main.language.MainLanguageViewModel
import com.android.greenapp.presentation.main.managewallet.ManageWalletViewModel
import com.android.greenapp.presentation.main.notification.NotifViewModel
import com.android.greenapp.presentation.main.receive.ReceiveViewModel
import com.android.greenapp.presentation.main.send.SendFragmentViewModel
import com.android.greenapp.presentation.main.support.SupportViewModel
import com.android.greenapp.presentation.main.transaction.TransactionsViewModel
import com.android.greenapp.presentation.main.walletlist.WalletListViewModel
import com.android.greenapp.presentation.main.walletsettings.WalletSettingsViewModel
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
    @ViewModelKey(SendFragmentViewModel::class)
    @AppScope
    abstract fun bindsSendFragmentViewModel(sendFragmentViewModel: SendFragmentViewModel): ViewModel


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




}
