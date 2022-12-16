package extralogic.wallet.greenapp.presentation.di.application

import android.app.Dialog
import extralogic.wallet.greenapp.presentation.onboard.OnBoardActivity
import extralogic.wallet.greenapp.presentation.onboard.icon.IconFragment
import extralogic.wallet.greenapp.presentation.onboard.language.LanguageFragment
import extralogic.wallet.greenapp.presentation.onboard.setpassword.SetPasswordFragment
import extralogic.wallet.greenapp.presentation.onboard.terms.TermsFragment
import extralogic.wallet.greenapp.presentation.intro.IntroActivity
import extralogic.wallet.greenapp.presentation.intro.authenticate.AuthFragmentIntro
import extralogic.wallet.greenapp.presentation.main.MainActivity
import extralogic.wallet.greenapp.presentation.main.about.AboutAppFragment
import extralogic.wallet.greenapp.presentation.main.address.AddressFragment
import extralogic.wallet.greenapp.presentation.main.address.EditAddressFragment
import extralogic.wallet.greenapp.presentation.main.allsettings.AllSettingsFragment
import extralogic.wallet.greenapp.presentation.main.authenticate.AuthenticateFragmentMain
import extralogic.wallet.greenapp.presentation.main.createnewwallet.CoinsDetailsFragment
import extralogic.wallet.greenapp.presentation.main.createnewwallet.ProgressCreatingWalletFragment
import extralogic.wallet.greenapp.presentation.main.createnewwallet.SaveMnemonicsFragment
import extralogic.wallet.greenapp.presentation.main.createnewwallet.VerificationFragment
import extralogic.wallet.greenapp.presentation.main.enterpasscode.EnterPasscodeFragment
import extralogic.wallet.greenapp.presentation.main.home.HomeFragment
import extralogic.wallet.greenapp.presentation.main.impmnemonics.ImpMnemonicFragment
import extralogic.wallet.greenapp.presentation.main.importtoken.ImportTokenFragment
import extralogic.wallet.greenapp.presentation.main.language.MainLanguageFragment
import extralogic.wallet.greenapp.presentation.main.managewallet.ManageWalletFragment
import extralogic.wallet.greenapp.presentation.main.notification.NotifFragment
import extralogic.wallet.greenapp.presentation.main.receive.ReceiveFragment
import extralogic.wallet.greenapp.presentation.main.scan.ScannerFragment
import extralogic.wallet.greenapp.presentation.main.send.SendFragment
import extralogic.wallet.greenapp.presentation.main.support.ListingFragment
import extralogic.wallet.greenapp.presentation.main.support.QuestionFragment
import extralogic.wallet.greenapp.presentation.main.support.SupportFragment
import extralogic.wallet.greenapp.presentation.main.support.faq.FAQFragment
import extralogic.wallet.greenapp.presentation.main.transaction.TransactionsFragment
import extralogic.wallet.greenapp.presentation.main.walletlist.AllWalletListFragment
import extralogic.wallet.greenapp.presentation.main.walletsettings.WalletSettingsFragment
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
