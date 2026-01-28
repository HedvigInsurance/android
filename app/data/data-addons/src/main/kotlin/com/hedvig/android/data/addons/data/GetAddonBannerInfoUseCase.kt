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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import octopus.TravelAddonBannerQuery
import octopus.type.UpsellTravelAddonFlow

interface GetAddonBannerInfoUseCase {
  fun invoke(source: AddonBannerSource): Flow<Either<ErrorMessage, List<AddonBannerInfo>>>
}

internal class GetAddonBannerInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetAddonBannerInfoUseCase {
  override fun invoke(source: AddonBannerSource): Flow<Either<ErrorMessage, List<AddonBannerInfo>>>{
    val mappedSource = when (source) {
      AddonBannerSource.TRAVEL_CERTIFICATES,
      AddonBannerSource.DEEPLINK,
        -> UpsellTravelAddonFlow.APP_UPSELL_UPGRADE

      AddonBannerSource.INSURANCES_TAB,

        //todo: add -> listOf(UpsellTravelAddonFlow.APP_CAR_PLUS,
        // UpsellTravelAddonFlow.APP_TRAVEL_PLUS_SELL_ONLY),

      AddonBannerSource.AFTER_FINISHING_SUCCESSFUL_FLOW,
        -> UpsellTravelAddonFlow.APP_ONLY_UPSALE
    }
    return combine(
      featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON),
      //todo: add featureManager.isFeatureEnabled(Feature.CAR_ADDON)
      apolloClient
        .query(TravelAddonBannerQuery(mappedSource))
        .fetchPolicy(CacheAndNetwork)
        .safeFlow()
        .map {
          it.mapLeft { error ->
            logcat(LogPriority.WARN, error) { "Error from travelAddonBannerQuery from source: $mappedSource: $error" }
            ErrorMessage()
          }
        },
    ) { isAddonFlagOn, travelAddonBannerQueryResult ->
      either {
        if (!isAddonFlagOn) {
          logcat(LogPriority.INFO) {
            "Tried to get TravelAddonBannerInfo but addon feature flag is off"
          }
          return@either emptyList()
        }
        val bannerData = travelAddonBannerQueryResult.bind().currentMember.upsellTravelAddonBanner
        if (bannerData == null) {
          logcat(LogPriority.DEBUG) { "Got null response from TravelAddonBannerQuery" }
          return@either emptyList()
        }
        val nonEmptyContracts = bannerData.contractIds.toNonEmptyListOrNull()
        if (nonEmptyContracts == null) {
          logcat(LogPriority.ERROR) {
            "Got non null response from TravelAddonBannerQuery from source: " +
              "$mappedSource, but contractIds are empty"
          }
          return@either emptyList()
        }
        listOf(AddonBannerInfo(
          title = bannerData.titleDisplayName,
          description = bannerData.descriptionDisplayName,
          labels = bannerData.badges,
          eligibleInsurancesIds = nonEmptyContracts,
        ))
      }
    }
  }
}

data class AddonBannerInfo(
  val title: String,
  val description: String,
  val labels: List<String>,
  val eligibleInsurancesIds: NonEmptyList<String>,
  //todo: add val flowType: FlowType
)

//todo:
//enum class FlowType {
//  APP_TRAVEL_PLUS_SELL_ONLY,
//  APP_TRAVEL_PLUS_SELL_OR_UPGRADE,
//  APP_CAR_PLUS
//}

@Serializable
@androidx.annotation.Keep
enum class AddonBannerSource {
  TRAVEL_CERTIFICATES,
  INSURANCES_TAB,
  AFTER_FINISHING_SUCCESSFUL_FLOW,
  DEEPLINK,
}
