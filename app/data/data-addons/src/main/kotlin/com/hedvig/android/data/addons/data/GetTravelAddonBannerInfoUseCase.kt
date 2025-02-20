package com.hedvig.android.data.addons.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy.CacheAndNetwork
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.String
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import octopus.TravelAddonBannerQuery
import octopus.type.UpsellTravelAddonFlow

interface GetTravelAddonBannerInfoUseCase {
  fun invoke(source: TravelAddonBannerSource): Flow<Either<ErrorMessage, TravelAddonBannerInfo?>>
}

internal class GetTravelAddonBannerInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetTravelAddonBannerInfoUseCase {
  override fun invoke(source: TravelAddonBannerSource): Flow<Either<ErrorMessage, TravelAddonBannerInfo?>> {
    val mappedSource = when (source) {
      TravelAddonBannerSource.TRAVEL_CERTIFICATES, TravelAddonBannerSource.DEEPLINK -> UpsellTravelAddonFlow.APP_UPSELL_UPGRADE
      TravelAddonBannerSource.INSURANCES_TAB -> UpsellTravelAddonFlow.APP_ONLY_UPSALE

    }
    return combine(
      featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON),
      apolloClient
        .query(TravelAddonBannerQuery(mappedSource))
        .fetchPolicy(CacheAndNetwork)
        .safeFlow()
        .map {
          it.mapLeft { error ->
            logcat(LogPriority.ERROR) { "Error from travelAddonBannerQuery from source: $mappedSource: $error" }
            ErrorMessage()
          }
        },
    ) { isAddonFlagOn, travelAddonBannerQueryResult ->
      either {
        if (!isAddonFlagOn) {
          logcat(LogPriority.INFO) {
            "Tried to get TravelAddonBannerInfo but addon feature flag is off"
          }
          return@either null
        }
        val bannerData = travelAddonBannerQueryResult.bind().currentMember.upsellTravelAddonBanner
        if (bannerData == null) {
          logcat(LogPriority.DEBUG) { "Got null response from TravelAddonBannerQuery" }
          return@either null
        }
        val nonEmptyContracts = bannerData.contractIds.toNonEmptyListOrNull()
        if (nonEmptyContracts == null) {
          logcat(LogPriority.ERROR) {
            "Got non null response from TravelAddonBannerQuery from source: " +
              "$mappedSource, but contractIds are empty"
          }
          return@either null
        }
        TravelAddonBannerInfo(
          title = bannerData.titleDisplayName,
          description = bannerData.descriptionDisplayName,
          labels = bannerData.badges,
          eligibleInsurancesIds = nonEmptyContracts,
          bannerSource = mappedSource,
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
  DEEPLINK
}
