package com.android.greenapp.presentation.di.application

import android.app.Dialog
import com.android.greenapp.presentation.greeting.GreetingActivity
import com.android.greenapp.presentation.greeting.icon.IconFragment
import com.android.greenapp.presentation.greeting.language.LanguageFragment
import com.android.greenapp.presentation.greeting.setpassword.SetPasswordFragment
import com.android.greenapp.presentation.greeting.terms.TermsFragment
import com.android.greenapp.presentation.intro.IntroActivity
import com.android.greenapp.presentation.intro.authenticate.AuthFragmentIntro
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.main.about.AboutAppFragment
import com.android.greenapp.presentation.main.address.AddressFragment
import com.android.greenapp.presentation.main.address.EditAddressFragment
import com.android.greenapp.presentation.main.allsettings.AllSettingsFragment
import com.android.greenapp.presentation.main.authenticate.AuthenticateFragmentMain
import com.android.greenapp.presentation.main.createnewwallet.CoinsDetailsFragment
import com.android.greenapp.presentation.main.createnewwallet.ProgressCreatingWalletFragment
import com.android.greenapp.presentation.main.createnewwallet.SaveMnemonicsFragment
import com.android.greenapp.presentation.main.createnewwallet.VerificationFragment
import com.android.greenapp.presentation.main.enterpasscode.EnterPasscodeFragment
import com.android.greenapp.presentation.main.home.HomeFragment
import com.android.greenapp.presentation.main.impmnemonics.ImpMnemonicFragment
import com.android.greenapp.presentation.main.importtoken.ImportTokenFragment
import com.android.greenapp.presentation.main.language.MainLanguageFragment
import com.android.greenapp.presentation.main.managewallet.ManageWalletFragment
import com.android.greenapp.presentation.main.notification.NotifFragment
import com.android.greenapp.presentation.main.receive.ReceiveFragment
import com.android.greenapp.presentation.main.scan.ScannerFragment
import com.android.greenapp.presentation.main.send.SendFragment
import com.android.greenapp.presentation.main.support.ListingFragment
import com.android.greenapp.presentation.main.support.QuestionFragment
import com.android.greenapp.presentation.main.support.SupportFragment
import com.android.greenapp.presentation.main.support.faq.FAQFragment
import com.android.greenapp.presentation.main.transaction.TransactionsFragment
import com.android.greenapp.presentation.main.walletlist.AllWalletListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class InjectorBuildersModule {

    @ContributesAndroidInjector
    abstract fun injectGreetingActivity(): GreetingActivity

    @ContributesAndroidInjector
    abstract fun injectIntroLanguageFragment(): LanguageFragment

    @ContributesAndroidInjector
    abstract fun injectThemeFragment(): IconFragment

    @ContributesAndroidInjector
    abstract fun injectSetPasswordFragment(): SetPasswordFragment

    @ContributesAndroidInjector
    abstract fun injectEntPasswordFragment(): AuthFragmentIntro

    @ContributesAndroidInjector
    abstract fun injectIntoIntroActivity(): IntroActivity

    @ContributesAndroidInjector
    abstract fun injectWalletFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun injectIntoMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun injectIntoImpMnemonic(): ImpMnemonicFragment

    @ContributesAndroidInjector()
    abstract fun injectIntoSettingDialog(): Dialog

    @ContributesAndroidInjector
    abstract fun injectIntoReceiveFragment(): ReceiveFragment

    @ContributesAndroidInjector
    abstract fun injectIntoSendFragment(): SendFragment

    @ContributesAndroidInjector
    abstract fun injectIntoScannerFragment(): ScannerFragment

    @ContributesAndroidInjector
    abstract fun injectIntoSaveMnemonicsFragment(): SaveMnemonicsFragment

    @ContributesAndroidInjector
    abstract fun injectIntoVerificationFragment(): VerificationFragment

    @ContributesAndroidInjector
    abstract fun injectIntoActivesFragment(): AllWalletListFragment

    @ContributesAndroidInjector
    abstract fun injectIntoEntPasscodeFragment(): EnterPasscodeFragment

    @ContributesAndroidInjector
    abstract fun injectIntoManageWalletFragment(): ManageWalletFragment

    @ContributesAndroidInjector
    abstract fun injectIntoCoinsDetailFragment(): CoinsDetailsFragment

    @ContributesAndroidInjector
    abstract fun injectIntoImportTokenFragment(): ImportTokenFragment

    @ContributesAndroidInjector
    abstract fun injectIntoSupportFragment(): SupportFragment

    @ContributesAndroidInjector
    abstract fun injectIntoAllSettingsFragment(): AllSettingsFragment

    @ContributesAndroidInjector
    abstract fun injectMainLanguageFragment(): MainLanguageFragment

    @ContributesAndroidInjector
    abstract fun injectIntoAboutFragment(): AboutAppFragment

    @ContributesAndroidInjector
    abstract fun injectIntoTransactionFragment(): TransactionsFragment

    @ContributesAndroidInjector
    abstract fun injectIntoAuthenticateFragment(): AuthenticateFragmentMain

    @ContributesAndroidInjector
    abstract fun injectIntoAddressFragment(): AddressFragment

    @ContributesAndroidInjector
    abstract fun injectIntoAddAddressFragment(): EditAddressFragment

    @ContributesAndroidInjector
    abstract fun injectIntoCreatingWalletFragment(): ProgressCreatingWalletFragment

    @ContributesAndroidInjector
    abstract fun injectIntoNotificationFragment(): NotifFragment

    @ContributesAndroidInjector
    abstract fun injectIntoFAQFragment(): FAQFragment

    @ContributesAndroidInjector
    abstract fun injectIntoAskQuestionFragment(): QuestionFragment

    @ContributesAndroidInjector
    abstract fun injectIntoListingFragment(): ListingFragment


    @ContributesAndroidInjector
    abstract fun injectTermsFragment(): TermsFragment





}