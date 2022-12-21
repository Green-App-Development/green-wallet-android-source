package com.green.wallet.presentation.di.application

import android.app.Dialog
import com.green.wallet.presentation.onboard.OnBoardActivity
import com.green.wallet.presentation.onboard.icon.IconFragment
import com.green.wallet.presentation.onboard.language.LanguageFragment
import com.green.wallet.presentation.onboard.setpassword.SetPasswordFragment
import com.green.wallet.presentation.onboard.terms.TermsFragment
import com.green.wallet.presentation.intro.IntroActivity
import com.green.wallet.presentation.intro.authenticate.AuthFragmentIntro
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.about.AboutAppFragment
import com.green.wallet.presentation.main.address.AddressFragment
import com.green.wallet.presentation.main.address.EditAddressFragment
import com.green.wallet.presentation.main.allsettings.AllSettingsFragment
import com.green.wallet.presentation.main.authenticate.AuthenticateFragmentMain
import com.green.wallet.presentation.main.createnewwallet.CoinsDetailsFragment
import com.green.wallet.presentation.main.createnewwallet.ProgressCreatingWalletFragment
import com.green.wallet.presentation.main.createnewwallet.SaveMnemonicsFragment
import com.green.wallet.presentation.main.createnewwallet.VerificationFragment
import com.green.wallet.presentation.main.enterpasscode.EnterPasscodeFragment
import com.green.wallet.presentation.main.home.HomeFragment
import com.green.wallet.presentation.main.impmnemonics.ImpMnemonicFragment
import com.green.wallet.presentation.main.importtoken.ImportTokenFragment
import com.green.wallet.presentation.main.language.MainLanguageFragment
import com.green.wallet.presentation.main.managewallet.ManageWalletFragment
import com.green.wallet.presentation.main.notification.NotifFragment
import com.green.wallet.presentation.main.receive.ReceiveFragment
import com.green.wallet.presentation.main.scan.ScannerFragment
import com.green.wallet.presentation.main.send.SendFragment
import com.green.wallet.presentation.main.support.ListingFragment
import com.green.wallet.presentation.main.support.QuestionFragment
import com.green.wallet.presentation.main.support.SupportFragment
import com.green.wallet.presentation.main.support.faq.FAQFragment
import com.green.wallet.presentation.main.transaction.TransactionsFragment
import com.green.wallet.presentation.main.walletlist.AllWalletListFragment
import com.green.wallet.presentation.main.walletsettings.WalletSettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class InjectorBuildersModule {

	@ContributesAndroidInjector
	abstract fun injectGreetingActivity(): OnBoardActivity

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

	@ContributesAndroidInjector
	abstract fun injectWalletSettingsFragment(): WalletSettingsFragment


}
