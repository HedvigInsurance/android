package com.hedvig.app.feature.home.model

import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.type.PayinMethodStatus
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature

class HomeItemsBuilder(
  private val featureManager: FeatureManager,
) {

  suspend fun buildItems(
    homeData: HomeQuery.Data,
  ): List<HomeModel> = when {
    homeData.isActive() -> buildActiveItems(homeData)
    homeData.isSwitching() && (homeData.isPending() || homeData.isActiveInFuture()) -> buildSwitchingItems(homeData)
    homeData.isPending() -> buildPendingItems(homeData)
    homeData.isActiveInFuture() -> buildActiveInFutureItems(homeData)
    homeData.isTerminated() -> buildTerminatedItems(homeData)
    else -> listOf(HomeModel.Error)
  }

  private suspend fun buildActiveItems(homeData: HomeQuery.Data): List<HomeModel> = buildList {
    addAll(listOfNotNull(*psaItems(homeData.importantMessages).toTypedArray()))
    add(HomeModel.BigText.Active(homeData.member.firstName ?: ""))
    val claimStatusCard: HomeModel.ClaimStatus? = claimStatusCardOrNull(homeData)
    if (claimStatusCard != null) {
      add(claimStatusCard)
      add(HomeModel.StartClaimOutlined.NewClaim)
    } else {
      add(HomeModel.StartClaimContained.FirstClaim)
    }
    add(HomeModel.HowClaimsWork(homeData.howClaimsWork))
    addAll(listOfNotNull(*upcomingRenewals(homeData.contracts).toTypedArray()))
    if (
      homeData.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP &&
      featureManager.isFeatureEnabled(Feature.CONNECT_PAYIN_REMINDER)
    ) {
      add(HomeModel.ConnectPayin(featureManager.getPaymentType()))
    }
    if (featureManager.isFeatureEnabled(Feature.COMMON_CLAIMS)) {
      add(HomeModel.Header(R.string.home_tab_common_claims_title))
      addAll(
        listOfNotNull(
          *commonClaimsItems(
            homeData.commonClaims,
            homeData.isEligibleToCreateClaim,
          ).toTypedArray(),
        ),
      )
    }

    add(HomeModel.Header(R.string.home_tab_editing_section_title))
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
      add(HomeModel.StartClaimOutlined.NewClaim)
    } else {
      add(HomeModel.StartClaimContained.FirstClaim)
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
    return NonEmptyList.fromList(successData.claimStatusCards)
      .map { claimStatusCardsQuery ->
        HomeModel.ClaimStatus(claimStatusCardsQuery.map(ClaimStatusCardUiState::fromClaimStatusCardsQuery))
      }
      .orNull()
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
  ) = commonClaims.map { cc ->
    cc.layout.asEmergency?.let {
      EmergencyData.from(cc, isEligibleToCreateClaim)?.let { ed ->
        return@map HomeModel.CommonClaim.Emergency(ed)
      }
    }
    cc.layout.asTitleAndBulletPoints?.let {
      CommonClaimsData.from(cc, isEligibleToCreateClaim)
        ?.let { ccd ->
          return@map HomeModel.CommonClaim.TitleAndBulletPoints(ccd)
        }
    }
    null
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

  private fun HomeQuery.Data.isSwitching() = contracts.any {
    insuranceProviders.map(HomeQuery.InsuranceProvider::id).contains(it.switchedFromInsuranceProvider)
  }
}
