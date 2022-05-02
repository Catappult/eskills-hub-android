package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import io.reactivex.Single
import javax.inject.Inject

class UpdateLastShownUserLevelUseCase @Inject constructor(private val promotionsRepository: PromotionsRepository) {

  operator fun invoke(address: String, currentLevel: Int) {
    return promotionsRepository.shownLevel(
      address, currentLevel,
      GamificationContext.NOTIFICATIONS_LEVEL_UP
    )
  }
}