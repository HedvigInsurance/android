package com.hedvig.android.feature.home.home

import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyData
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.app.feature.home.model.CommonClaim
import com.hedvig.app.feature.home.model.HomeModel
import giraffe.HomeQuery
import giraffe.type.PayinMethodStatus

internal class HomeItemsBuilder(
  private val featureManager: FeatureManager,
) {
  suspend fun buildItems(
    homeData: HomeQuery.Data,
    showTravelCertificate: Boolean,
  ): List<HomeModel> = when {
    homeData.isActive() -> buildActiveItems(homeData, showTravelCertificate)
    homeData.isSwitching() && (homeData.isPending() || homeData.isActiveInFuture()) -> buildSwitchingItems(homeData)
    homeData.isPending() -> buildPendingItems(homeData)
    homeData.isActiveInFuture() -> buildActiveInFutureItems(homeData)
    homeData.isTerminated() -> buildTerminatedItems(homeData)
    else -> emptyList()
  }

  private suspend fun buildActiveItems(
    homeData: HomeQuery.Data,
    showTravelCertificate: Boolean,
  ): List<HomeModel> = buildList {
    addAll(listOfNotNull(*psaItems(homeData.importantMessages).toTypedArray()))
    add(HomeModel.BigText.Active(homeData.member.firstName ?: ""))
    val claimStatusCard: HomeModel.ClaimStatus? = claimStatusCardOrNull(homeData)
    if (claimStatusCard != null) {
      add(claimStatusCard)
      add(HomeModel.StartClaim.NewClaim)
    } else {
      add(HomeModel.StartClaim.FirstClaim)
    }
    add(HomeModel.HowClaimsWork(homeData.howClaimsWork))
    addAll(listOfNotNull(*upcomingRenewals(homeData.contracts).toTypedArray()))
    if (
      homeData.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP &&
      featureManager.isFeatureEnabled(Feature.CONNECT_PAYIN_REMINDER)
    ) {
      add(HomeModel.ConnectPayin(featureManager.getPaymentType()))
    }
    val commonClaims: MutableList<CommonClaim> = mutableListOf()
    if (featureManager.isFeatureEnabled(Feature.COMMON_CLAIMS) && homeData.commonClaims.isNotEmpty()) {
      commonClaims.addAll(
        commonClaimsItems(
          homeData.commonClaims,
          homeData.isEligibleToCreateClaim,
        ),
      )
    }

    if (showTravelCertificate) {
      commonClaims.add(CommonClaim.GenerateTravelCertificate)
    }
    val nonEmptyCommonClaimsList = commonClaims.toNonEmptyListOrNull()
    if (nonEmptyCommonClaimsList != null) {
      add(HomeModel.Header(hedvig.resources.R.string.home_tab_common_claims_title))
      add(HomeModel.CommonClaims(nonEmptyCommonClaimsList))
    }

    add(HomeModel.Header(hedvig.resources.R.string.home_tab_editing_section_title))
    add(HomeModel.ChangeAddress)
  }

  private fun buildActiveInFutureItems(homeData: HomeQuery.Data): List<HomeModel> = buildList {
    val firstInceptionDate = homeData
      .contracts
      .mapNotNull {
        it.status.asActiveInFutureStatus?.futureInception
          ?: it.status.asActiveInFutureAndTerminatedInFutureStatus?.futureInception
      }
      .minOrNull()

    if (firstInceptionDate != null) {
      add(HomeModel.BigText.ActiveInFuture(homeData.member.firstName ?: "", firstInceptionDate))
    }
    add(HomeModel.BodyText.ActiveInFuture)
    claimStatusCardOrNull(homeData)?.let(::add)
  }

  private fun buildTerminatedItems(homeData: HomeQuery.Data): List<HomeModel> = buildList {
    add(HomeModel.BigText.Terminated(homeData.member.firstName ?: ""))
    add(HomeModel.BodyText.Terminated)
    val claimStatusCard: HomeModel.ClaimStatus? = claimStatusCardOrNull(homeData)
    if (claimStatusCard != null) {
      add(HomeModel.Space(24.dp))
      add(claimStatusCard)
      add(HomeModel.StartClaim.NewClaim)
    } else {
      add(HomeModel.StartClaim.FirstClaim)
    }
    add(HomeModel.HowClaimsWork(homeData.howClaimsWork))
  }

  private fun buildPendingItems(homeData: HomeQuery.Data): List<HomeModel> = buildList {
    add(HomeModel.BigText.Pending(homeData.member.firstName ?: ""))
    add(HomeModel.BodyText.Pending)
    claimStatusCardOrNull(homeData)?.let(::add)
  }

  private fun buildSwitchingItems(homeData: HomeQuery.Data): List<HomeModel> = buildList {
    add(HomeModel.BigText.Switching(homeData.member.firstName ?: ""))
    add(HomeModel.BodyText.Switching)
    claimStatusCardOrNull(homeData)?.let(::add)
  }

  private fun claimStatusCardOrNull(successData: HomeQuery.Data): HomeModel.ClaimStatus? {
    val claimStatusCards: NonEmptyList<HomeQuery.ClaimStatusCard> =
      successData.claimStatusCards.toNonEmptyListOrNull() ?: return null
    return HomeModel.ClaimStatus(claimStatusCards.map(ClaimStatusCardUiState::fromClaimStatusCardsQuery))
  }

  private fun psaItems(
    importantMessages: List<HomeQuery.ImportantMessage?>,
  ) = importantMessages
    .filterNotNull()
    .map { HomeModel.PSA(it) }

  private fun upcomingRenewals(contracts: List<HomeQuery.Contract>): List<HomeModel.UpcomingRenewal> =
    contracts.mapNotNull { c ->
      c.upcomingRenewal?.let {
        HomeModel.UpcomingRenewal(c.displayName, it)
      }
    }

  private fun commonClaimsItems(
    commonClaims: List<HomeQuery.CommonClaim>,
    isEligibleToCreateClaim: Boolean,
  ): List<CommonClaim> {
    val commonClaimsResult: List<CommonClaim> = commonClaims.mapNotNull { cc ->
      cc.layout.asEmergency?.let {
        EmergencyData.from(cc, isEligibleToCreateClaim)?.let { ed ->
          return@mapNotNull CommonClaim.Emergency(ed)
        }
      }
      cc.layout.asTitleAndBulletPoints?.let {
        CommonClaimsData.from(cc, isEligibleToCreateClaim)
          ?.let { ccd ->
            return@mapNotNull CommonClaim.TitleAndBulletPoints(ccd)
          }
      }
      null
    }
    return commonClaimsResult
  }

  private fun HomeQuery.Data.isPending() = contracts.all { it.status.asPendingStatus != null }

  private fun HomeQuery.Data.isActiveInFuture() = contracts.all {
    it.status.asActiveInFutureStatus != null ||
      it.status.asActiveInFutureAndTerminatedInFutureStatus != null
  }

  private fun HomeQuery.Data.isActive() = contracts.any {
    it.status.asActiveStatus != null ||
      it.status.asTerminatedTodayStatus != null ||
      it.status.asTerminatedInFutureStatus != null
  }

  private fun HomeQuery.Data.isTerminated() = contracts.all { it.status.asTerminatedStatus != null }

  private fun HomeQuery.Data.isSwitching() = contracts.any { contract ->
    val switchedFromProvider = insuranceProviders.firstOrNull {
      it.id == contract.switchedFromInsuranceProvider
    }

    switchedFromProvider?.switchable ?: false
  }
}
