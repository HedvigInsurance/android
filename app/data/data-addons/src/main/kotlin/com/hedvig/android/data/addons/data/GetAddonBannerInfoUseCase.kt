package com.hedvig.android.data.addons.data

import androidx.annotation.Keep
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
import octopus.AddonBannersQuery
import octopus.type.AddonFlow

interface GetAddonBannerInfoUseCase {
  fun invoke(source: AddonBannerSource): Flow<Either<ErrorMessage, List<AddonBannerInfo>>>
}

internal class GetAddonBannerInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetAddonBannerInfoUseCase {
  override fun invoke(source: AddonBannerSource): Flow<Either<ErrorMessage, List<AddonBannerInfo>>> {
    val mappedSource = when (source) {
      AddonBannerSource.TRAVEL_CERTIFICATES,
      AddonBannerSource.TRAVEL_DEEPLINK,
      -> listOf(AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE)

      AddonBannerSource.AFTER_FINISHING_SUCCESSFUL_FLOW,
      AddonBannerSource.INSURANCES_TAB,
      -> listOf(
        AddonFlow.APP_CAR_PLUS,
        AddonFlow.APP_TRAVEL_PLUS_SELL_ONLY,
      )

      AddonBannerSource.CAR_ADDON_DEEPLINK -> listOf(AddonFlow.APP_CAR_PLUS)
    }
    return combine(
      featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON),
      apolloClient
        .query(AddonBannersQuery(mappedSource))
        .fetchPolicy(CacheAndNetwork)
        .safeFlow()
        .map {
          it.mapLeft { error ->
            logcat(LogPriority.WARN, error) {
              "Error from AddonBannersQuery " +
                "from source: $mappedSource: $error"
            }
            ErrorMessage()
          }
        },
    ) { isAddonFlagOn, addonBannersQueryResult ->
      either {
        if (!isAddonFlagOn) {
          logcat(LogPriority.INFO) {
            "Tried AddonBannersQuery but travel addon feature flag is off"
          }
          return@either emptyList()
        }
        val bannerData = addonBannersQueryResult.bind().currentMember.addonBanners
        if (bannerData.isEmpty()) {
          logcat(LogPriority.DEBUG) { "Got empty response from AddonBannersQuery" }
          return@either emptyList()
        }
        bannerData.mapNotNull { banner ->
          val flowType = when (banner.flow) {
            AddonFlow.APP_TRAVEL_PLUS_SELL_ONLY -> FlowType.APP_TRAVEL_PLUS_SELL_ONLY
            AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE -> FlowType.APP_TRAVEL_PLUS_SELL_OR_UPGRADE
            AddonFlow.APP_CAR_PLUS -> FlowType.APP_CAR_PLUS
            AddonFlow.UNKNOWN__ -> null
          }
          val eligibleInsurancesIds = banner.contractIds.toNonEmptyListOrNull()
          if (flowType == null || eligibleInsurancesIds == null) {
            logcat(LogPriority.DEBUG) {
              "Got AddonFlow.UNKNOWN or empty contractIds from AddonBannersQuery"
            }
            null
          } else {
            AddonBannerInfo(
              title = banner.displayTitleName,
              description = banner.descriptionDisplayName,
              labels = banner.badges,
              eligibleInsurancesIds = eligibleInsurancesIds,
              flowType = flowType,
            )
          }
        }
      }
    }
  }
}

data class AddonBannerInfo(
  val title: String,
  val description: String,
  val labels: List<String>,
  val eligibleInsurancesIds: NonEmptyList<String>,
  val flowType: FlowType,
)

enum class FlowType {
  APP_TRAVEL_PLUS_SELL_ONLY,
  APP_TRAVEL_PLUS_SELL_OR_UPGRADE,
  APP_CAR_PLUS,
}

@Serializable
@Keep
enum class AddonBannerSource {
  TRAVEL_CERTIFICATES,
  INSURANCES_TAB,
  AFTER_FINISHING_SUCCESSFUL_FLOW,
  TRAVEL_DEEPLINK,
  CAR_ADDON_DEEPLINK,
}
