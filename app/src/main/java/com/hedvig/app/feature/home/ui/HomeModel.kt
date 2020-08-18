package com.hedvig.app.feature.home.ui

import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import java.time.LocalDate

sealed class HomeModel {
    sealed class BigText : HomeModel() {
        data class Pending(
            val name: String
        ) : BigText()

        data class ActiveInFuture(
            val name: String,
            val inception: LocalDate
        ) : BigText()

        data class Active(
            val name: String
        ) : BigText()

        data class Terminated(
            val name: String
        ) : BigText()
    }

    sealed class BodyText : HomeModel() {
        object Pending : BodyText()
        object ActiveInFuture : BodyText()
    }

    object StartClaimOutlined : HomeModel()

    object StartClaimContained : HomeModel()

    object CommonClaimTitle : HomeModel()

    object Error : HomeModel()

    sealed class CommonClaim : HomeModel() {
        data class Emergency(
            val title: String
        ) : CommonClaim()

        data class TitleAndBulletPoints(
            val inner: CommonClaimsData
        ) : CommonClaim()
    }
}
