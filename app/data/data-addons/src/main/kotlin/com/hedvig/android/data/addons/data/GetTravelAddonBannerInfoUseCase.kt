package com.hedvig.android.data.addons.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.String
import kotlinx.coroutines.flow.first
import octopus.TravelAddonBannerQuery
import octopus.type.UpsellTravelAddonFlow

interface GetTravelAddonBannerInfoUseCase {
  suspend fun invoke(source: TravelAddonBannerSource): Either<ErrorMessage, TravelAddonBannerInfo?>
}

internal class GetTravelAddonBannerInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetTravelAddonBannerInfoUseCase {
  override suspend fun invoke(source: TravelAddonBannerSource): Either<ErrorMessage, TravelAddonBannerInfo?> {
    return either {
      val isAddonFlagOn = featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON).first()
      if (!isAddonFlagOn) {
        logcat(LogPriority.INFO) {
          "Tried to get TravelAddonBannerInfo but addon feature flag is off"
        }
        null
      } else {
//        TravelAddonBannerInfo(
//          title = "Travel Plus",
//          description = "Extended travel insurance with extra coverage for your travels",
//          labels = listOf("Popular"),
//          eligibleInsurancesIds = nonEmptyListOf("id1"),
//        ) //todo: remove mock
        val mappedSource = when (source) {
          TravelAddonBannerSource.TRAVEL_CERTIFICATES -> UpsellTravelAddonFlow.APP_UPSELL_UPGRADE
          TravelAddonBannerSource.INSURANCES_TAB -> UpsellTravelAddonFlow.APP_ONLY_UPSALE
        }
        apolloClient.query(TravelAddonBannerQuery(mappedSource)).safeExecute().fold(
          ifLeft = { error ->
            logcat(LogPriority.ERROR) { "Error from travelAddonBannerQuery from source: $mappedSource: $error" }
            raise(ErrorMessage())
          },
          ifRight = { result ->
            val bannerData = result.currentMember.upsellTravelAddonBanner
            if (bannerData == null) {
              logcat(LogPriority.DEBUG) { "Got null response from TravelAddonBannerQuery" }
              null
            } else {
              val nonEmptyContracts = bannerData.contractIds.toNonEmptyListOrNull()
              if (nonEmptyContracts.isNullOrEmpty()) {
                logcat(LogPriority.ERROR) {
                  "Got non null response from TravelAddonBannerQuery from source: " +
                    "$mappedSource, but contractIds are empty"
                }
                null
              } else {
                TravelAddonBannerInfo(
                  title = bannerData.titleDisplayName,
                  description = bannerData.descriptionDisplayName,
                  labels = bannerData.badges,
                  eligibleInsurancesIds = nonEmptyContracts,
                  bannerSource = mappedSource,
                )
              }
            }
          },
        )
      }
    }
  }
}

data class TravelAddonBannerInfo(
  val title: String,
  val description: String,
  val labels: List<String>,
  val eligibleInsurancesIds: NonEmptyList<String>,
  val bannerSource: UpsellTravelAddonFlow,
)

enum class TravelAddonBannerSource {
  TRAVEL_CERTIFICATES,
  INSURANCES_TAB,
}
