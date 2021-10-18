package com.asfoundation.wallet.di

import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.biometric.BiometricManager
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import com.asfoundation.wallet.backup.BackupInteractContract
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.eskills_withdraw.repository.WithdrawRepository
import com.asfoundation.wallet.eskills_withdraw.use_cases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills_withdraw.use_cases.WithdrawToFiatUseCase
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.gamification.ObserveLevelsUseCase
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.main.usecases.HasSeenPromotionTooltipUseCase
import com.asfoundation.wallet.main.usecases.IncreaseLaunchCountUseCase
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.promotions.model.PromotionsMapper
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenWalletOriginUseCase
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.rating.RatingRepository
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.SharedPreferencesReferralLocalData
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.balance.BalanceRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UseCaseModule {
  @Singleton
  @Provides
  fun providesGetPromotionsUseCase(getCurrentWallet: GetCurrentWalletUseCase,
                                   observeLevels: ObserveLevelsUseCase,
                                   promotionsMapper: PromotionsMapper,
                                   promotionsRepository: PromotionsRepository): GetPromotionsUseCase {
    return GetPromotionsUseCase(getCurrentWallet, observeLevels, promotionsMapper,
        promotionsRepository)
  }

  @Singleton
  @Provides
  fun providesGetCurrentWalletUseCase(
      walletRepository: WalletRepositoryType): GetCurrentWalletUseCase {
    return GetCurrentWalletUseCase(walletRepository)
  }

  @Singleton
  @Provides
  fun providesObserveLevelsUseCase(getCurrentWallet: GetCurrentWalletUseCase,
                                   gamification: Gamification): ObserveLevelsUseCase {
    return ObserveLevelsUseCase(getCurrentWallet, gamification)
  }

  @Singleton
  @Provides
  fun providesSetSeenWalletOriginUseCase(
      userStatsLocalData: UserStatsLocalData): SetSeenWalletOriginUseCase {
    return SetSeenWalletOriginUseCase(userStatsLocalData)
  }

  @Singleton
  @Provides
  fun providesSetSeenPromotionsUseCase(
      promotionsRepository: PromotionsRepository): SetSeenPromotionsUseCase {
    return SetSeenPromotionsUseCase(promotionsRepository)
  }

  @Singleton
  @Provides
  fun providesHasSeenPromotionTooltipUseCase(
      preferencesRepositoryType: PreferencesRepositoryType): HasSeenPromotionTooltipUseCase {
    return HasSeenPromotionTooltipUseCase(preferencesRepositoryType)
  }

  @Singleton
  @Provides
  fun providesIncreaseLaunchTimesUseCase(
      preferencesRepositoryType: PreferencesRepositoryType): IncreaseLaunchCountUseCase {
    return IncreaseLaunchCountUseCase(preferencesRepositoryType)
  }

  /*
   HOME Use Cases
   */

  @Singleton
  @Provides
  fun providesShouldOpenRatingDialogUseCase(ratingRepository: RatingRepository,
                                            getUserLevelUseCase: GetUserLevelUseCase): ShouldOpenRatingDialogUseCase {
    return ShouldOpenRatingDialogUseCase(ratingRepository, getUserLevelUseCase)
  }

  @Singleton
  @Provides
  fun providesUpdateTransactionsNumberUseCase(
      ratingRepository: RatingRepository): UpdateTransactionsNumberUseCase {
    return UpdateTransactionsNumberUseCase(ratingRepository)
  }

  @Singleton
  @Provides
  fun providesFindNetworkInfoUseCase(networkInfo: NetworkInfo): FindNetworkInfoUseCase {
    return FindNetworkInfoUseCase(networkInfo)
  }

  @Singleton
  @Provides
  fun providesFetchTransactionsUseCase(
      transactionRepository: TransactionRepositoryType): FetchTransactionsUseCase {
    return FetchTransactionsUseCase(transactionRepository)
  }

  @Singleton
  @Provides
  fun providesFindDefaultWalletUseCase(
      walletRepository: WalletRepositoryType): FindDefaultWalletUseCase {
    return FindDefaultWalletUseCase(walletRepository)
  }

  @Singleton
  @Provides
  fun providesObserveDefaultWalletUseCase(
      walletRepository: WalletRepositoryType): ObserveDefaultWalletUseCase {
    return ObserveDefaultWalletUseCase(walletRepository)
  }

  @Singleton
  @Provides
  fun providesDismissCardNotificationUseCase(findDefaultWalletUseCase: FindDefaultWalletUseCase,
                                             preferences: SharedPreferences,
                                             autoUpdateRepository: AutoUpdateRepository,
                                             sharedPreferencesRepository: PreferencesRepositoryType,
                                             backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
                                             promotionsRepo: PromotionsRepository): DismissCardNotificationUseCase {
    return DismissCardNotificationUseCase(findDefaultWalletUseCase,
        SharedPreferencesReferralLocalData(preferences),
        autoUpdateRepository, sharedPreferencesRepository, backupRestorePreferencesRepository,
        promotionsRepo)
  }

  @Singleton
  @Provides
  fun providesShouldShowFingerprintTooltipUseCase(
      preferencesRepositoryType: PreferencesRepositoryType,
      packageManager: PackageManager,
      fingerprintPreferences: FingerprintPreferencesRepositoryContract,
      biometricManager: BiometricManager): ShouldShowFingerprintTooltipUseCase {
    return ShouldShowFingerprintTooltipUseCase(preferencesRepositoryType, packageManager,
        fingerprintPreferences, biometricManager)
  }

  @Singleton
  @Provides
  fun providesSetSeenFingerprintTooltipUseCase(
      fingerprintPreferences: FingerprintPreferencesRepositoryContract): SetSeenFingerprintTooltipUseCase {
    return SetSeenFingerprintTooltipUseCase(fingerprintPreferences)
  }

  @Singleton
  @Provides
  fun providesGetLevelsUseCase(gamification: Gamification,
                               findDefaultWalletUseCase: FindDefaultWalletUseCase): GetLevelsUseCase {
    return GetLevelsUseCase(gamification, findDefaultWalletUseCase)
  }

  @Singleton
  @Provides
  fun providesGetUserLevelUseCase(gamification: Gamification,
                                  findDefaultWalletUseCase: FindDefaultWalletUseCase): GetUserLevelUseCase {
    return GetUserLevelUseCase(gamification, findDefaultWalletUseCase)
  }

  @Singleton
  @Provides
  fun providesGetAppcBalanceUseCase(getCurrentWalletUseCase: GetCurrentWalletUseCase,
                                    balanceRepository: BalanceRepository): GetAppcBalanceUseCase {
    return GetAppcBalanceUseCase(getCurrentWalletUseCase, balanceRepository)
  }

  @Singleton
  @Provides
  fun providesGetEthBalanceUseCase(getCurrentWalletUseCase: GetCurrentWalletUseCase,
                                   balanceRepository: BalanceRepository): GetEthBalanceUseCase {
    return GetEthBalanceUseCase(getCurrentWalletUseCase, balanceRepository)
  }

  @Singleton
  @Provides
  fun providesGetCreditsBalanceUseCase(getCurrentWalletUseCase: GetCurrentWalletUseCase,
                                       balanceRepository: BalanceRepository): GetCreditsBalanceUseCase {
    return GetCreditsBalanceUseCase(getCurrentWalletUseCase, balanceRepository)
  }

  @Singleton
  @Provides
  fun providesGetCardNotificationsUseCase(referralInteractor: ReferralInteractorContract,
                                          autoUpdateInteract: AutoUpdateInteract,
                                          backupInteract: BackupInteractContract,
                                          promotionsInteractor: PromotionsInteractor): GetCardNotificationsUseCase {
    return GetCardNotificationsUseCase(referralInteractor, autoUpdateInteract, backupInteract,
        promotionsInteractor)
  }

  @Singleton
  @Provides
  fun providesRegisterSupportUserUseCase(
      supportRepository: SupportRepository): RegisterSupportUserUseCase {
    return RegisterSupportUserUseCase(supportRepository)
  }

  @Singleton
  @Provides
  fun provideGetUnreadConversationsCountEventsUseCase() =
      GetUnreadConversationsCountEventsUseCase()

  @Singleton
  @Provides
  fun providesDisplayChatUseCase(supportRepository: SupportRepository): DisplayChatUseCase {
    return DisplayChatUseCase(supportRepository)
  }

  @Singleton
  @Provides
  fun providesDisplayConversationListOrChatUseCase(
      supportRepository: SupportRepository): DisplayConversationListOrChatUseCase {
    return DisplayConversationListOrChatUseCase(supportRepository)
  }

  @Singleton
  @Provides
  fun providesSetSelectedCurrencyUseCase(
      fiatCurrenciesRepository: FiatCurrenciesRepository): SetSelectedCurrencyUseCase {
    return SetSelectedCurrencyUseCase(fiatCurrenciesRepository)
  }

  @Singleton
  @Provides
  fun providesGetChangeFiatCurrencyModelUseCase(fiatCurrenciesRepository: FiatCurrenciesRepository,
                                         conversionService: LocalCurrencyConversionService): GetChangeFiatCurrencyModelUseCase {
    return GetChangeFiatCurrencyModelUseCase(fiatCurrenciesRepository, conversionService)
  }

  @Singleton
  @Provides
  fun providesGetSelectedCurrencyUseCase(fiatCurrenciesRepository: FiatCurrenciesRepository): GetSelectedCurrencyUseCase {
    return GetSelectedCurrencyUseCase(fiatCurrenciesRepository)
  }

  @Singleton
  @Provides
  fun providesGetAvailableAmountToWithdrawUseCase(
      ewt: EwtAuthenticatorService,
      withdrawRepository: WithdrawRepository
  ): GetAvailableAmountToWithdrawUseCase {
    return GetAvailableAmountToWithdrawUseCase(ewt, withdrawRepository)
  }

  @Singleton
  @Provides
  fun providesWithdrawToFiatUseCase(
      ewt: EwtAuthenticatorService,
      withdrawRepository: WithdrawRepository
  ): WithdrawToFiatUseCase {
    return WithdrawToFiatUseCase(ewt, withdrawRepository)
  }
}
