package com.hedvig.android.data.addons.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.String
import kotlinx.coroutines.flow.first

interface GetTravelAddonBannerInfoUseCase {
  suspend fun invoke(): Either<ErrorMessage, TravelAddonBannerInfo?>
}

internal class GetTravelAddonBannerInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
): GetTravelAddonBannerInfoUseCase {
  override suspend fun invoke(): Either<ErrorMessage, TravelAddonBannerInfo?> {
    return either {
      val isAddonOn = featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON).first()
      if (!isAddonOn) {
        logcat(LogPriority.INFO) {
          "Tried to get TravelAddonBannerInfo but addon feature flag is off"
        }
        null
      } else {
        //TODO: actual impl here!!!!
        //TODO:  and null if eligibleInsurancesIds is empty
        TravelAddonBannerInfo(
          title = "Travel Plus",
          description = "Extended travel insurance with extra coverage for your travels",
          labels = listOf("Popular"),
          eligibleInsurancesIds = listOf(),
        )
      }
    }
  }
}

//todo: add IMPL

data class TravelAddonBannerInfo(
  val title: String,
  val description: String,
  val labels: List<String>,
  val eligibleInsurancesIds: List<String>,
)
