package com.hedvig.app.feature.home.model

import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager

@OptIn(ExperimentalStdlibApi::class)
class HomeItemsBuilder(
    private val featureManager: FeatureManager
) {

    fun buildItems(
        homeData: HomeQuery.Data,
        needsPayinSetup: Boolean
    ): List<HomeModel> = when {
        homeData.contracts.isActive() -> buildActiveItems(homeData, needsPayinSetup)
        homeData.contracts.isPending() -> buildPendingItems(homeData)
        homeData.contracts.isActiveInFuture() -> buildActiveInFutureItems(homeData)
        homeData.contracts.isTerminated() -> buildTerminatedItems(homeData)
        else -> listOf(HomeModel.Error)
    }

    private fun buildActiveItems(homeData: HomeQuery.Data, needsPayinSetup: Boolean): List<HomeModel> = buildList {
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
        if (needsPayinSetup) {
            add(HomeModel.ConnectPayin)
        }
        add(HomeModel.Header(R.string.home_tab_common_claims_title))
        addAll(
            listOfNotNull(
                *commonClaimsItems(
                    homeData.commonClaims,
                    homeData.isEligibleToCreateClaim
                ).toTypedArray()
            )
        )
        if (featureManager.isFeatureEnabled(Feature.MOVING_FLOW)) {
            add(HomeModel.Header(R.string.home_tab_editing_section_title))
            add(HomeModel.ChangeAddress)
        }
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
        if (featureManager.isFeatureEnabled(Feature.MOVING_FLOW)) {
            add(HomeModel.Header(R.string.home_tab_editing_section_title))
            add(HomeModel.ChangeAddress)
        }
    }

    private fun buildPendingItems(homeData: HomeQuery.Data): List<HomeModel> = buildList {
        add(HomeModel.BigText.Pending(homeData.member.firstName ?: ""))
        add(HomeModel.BodyText.Pending)
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
    ) =
        commonClaims.map { cc ->
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

    private fun List<HomeQuery.Contract>.isPending() = all { it.status.asPendingStatus != null }

    private fun List<HomeQuery.Contract>.isActiveInFuture() = all {
        it.status.asActiveInFutureStatus != null ||
            it.status.asActiveInFutureAndTerminatedInFutureStatus != null
    }

    private fun List<HomeQuery.Contract>.isActive() = any {
        it.status.asActiveStatus != null ||
            it.status.asTerminatedTodayStatus != null ||
            it.status.asTerminatedInFutureStatus != null
    }

    private fun List<HomeQuery.Contract>.isTerminated() = all { it.status.asTerminatedStatus != null }

    private fun List<HomeQuery.Contract>.isSwitching() = all { it.switchedFromInsuranceProvider != null }
}
