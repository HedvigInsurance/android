package com.hedvig.app.feature.home.model

import androidx.annotation.StringRes
import androidx.compose.ui.unit.Dp
import arrow.core.NonEmptyList
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.hanalytics.PaymentType
import java.time.LocalDate

sealed class HomeModel {
  sealed class BigText : HomeModel() {
    data class Switching(
      val name: String,
    ) : BigText()

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
    object Switching : BodyText()
  }

  data class ClaimStatus(
    val claimStatusCardsUiState: NonEmptyList<ClaimStatusCardUiState>,
  ) : HomeModel()

  data class Space(val height: Dp) : HomeModel()

  sealed class StartClaimOutlined : HomeModel() {
    @get:StringRes
    abstract val textId: Int

    object FirstClaim : StartClaimOutlined() {
      override val textId: Int
        get() = hedvig.resources.R.string.home_tab_claim_button_text
    }

    object NewClaim : StartClaimOutlined() {
      override val textId: Int
        get() = hedvig.resources.R.string.home_open_claim_start_new_claim_button
    }
  }

  sealed class StartClaimContained : HomeModel() {
    @get:StringRes
    abstract val textId: Int

    object FirstClaim : StartClaimContained() {
      override val textId: Int
        get() = hedvig.resources.R.string.home_tab_claim_button_text
    }

    object NewClaim : StartClaimContained() {
      override val textId: Int
        get() = hedvig.resources.R.string.home_open_claim_start_new_claim_button
    }
  }

  data class ConnectPayin(
    val payinType: PaymentType,
  ) : HomeModel()

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

  data class Header(val stringRes: Int) : HomeModel()

  data class PendingAddressChange(val address: String) : HomeModel()

  object ChangeAddress : HomeModel()
}
