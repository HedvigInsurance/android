package com.hedvig.app.feature.home.ui

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusData
import java.time.LocalDate

sealed class HomeModel {
    sealed class BigText : HomeModel() {
        data class Pending(
            val name: String,
        ) : BigText()

        data class ActiveInFuture(
            val name: String,
            val inception: LocalDate,
        ) : BigText()

        data class Active(
            val name: String,
        ) : BigText()

        data class Terminated(
            val name: String,
        ) : BigText()
    }

    data class UpcomingRenewal(
        val contractDisplayName: String,
        val upcomingRenewal: HomeQuery.UpcomingRenewal,
    ) : HomeModel()

    sealed class BodyText : HomeModel() {
        object Pending : BodyText()
        object ActiveInFuture : BodyText()
        object Terminated : BodyText()
    }

    data class ClaimStatus(
        val claimStatusDataList: List<ClaimStatusData>
    ) : HomeModel()

    object StartClaimOutlined : HomeModel()

    object StartClaimContained : HomeModel()

    object ConnectPayin : HomeModel()

    data class PSA(val inner: HomeQuery.ImportantMessage) : HomeModel()

    data class HowClaimsWork(val pages: List<HomeQuery.HowClaimsWork>) : HomeModel()

    object Error : HomeModel()

    sealed class CommonClaim : HomeModel() {
        data class Emergency(
            val inner: EmergencyData,
        ) : CommonClaim()

        data class TitleAndBulletPoints(
            val inner: CommonClaimsData,
        ) : CommonClaim()
    }

    data class Header(val text: String) : HomeModel()

    data class PendingAddressChange(val address: String) : HomeModel()

    data class ChangeAddress(val pendingAddress: String?) : HomeModel()
}
