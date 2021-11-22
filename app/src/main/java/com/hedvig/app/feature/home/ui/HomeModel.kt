package com.hedvig.app.feature.home.ui

import androidx.annotation.StringRes
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusCardData
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
        val claimStatusCardDataList: List<ClaimStatusCardData>,
    ) : HomeModel()

    sealed class StartClaimOutlined : HomeModel() {
        @get:StringRes abstract val textId: Int

        object FirstClaim : StartClaimOutlined() {
            override val textId: Int
                get() = R.string.home_tab_claim_button_text
        }

        object NewClaim : StartClaimOutlined() {
            override val textId: Int
                get() = R.string.home_open_claim_start_new_claim_button
        }
    }

    sealed class StartClaimContained : HomeModel() {
        @get:StringRes abstract val textId: Int

        object FirstClaim : StartClaimOutlined() {
            override val textId: Int
                get() = R.string.home_tab_claim_button_text
        }

        object NewClaim : StartClaimOutlined() {
            override val textId: Int
                get() = R.string.home_open_claim_start_new_claim_button
        }
    }

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
