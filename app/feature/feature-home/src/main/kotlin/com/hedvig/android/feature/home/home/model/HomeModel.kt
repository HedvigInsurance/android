package com.hedvig.app.feature.home.model

import androidx.annotation.StringRes
import androidx.compose.ui.unit.Dp
import arrow.core.NonEmptyList
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyData
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.hanalytics.PaymentType
import giraffe.HomeQuery
import java.time.LocalDate

internal sealed interface HomeModel {
  sealed interface BigText : HomeModel {
    val name: String

    data class Switching(
      override val name: String,
    ) : BigText

    data class Pending(
      override val name: String,
    ) : BigText

    data class ActiveInFuture(
      override val name: String,
      val inception: LocalDate,
    ) : BigText

    data class Active(
      override val name: String,
    ) : BigText

    data class Terminated(
      override val name: String,
    ) : BigText
  }

  data class UpcomingRenewal(
    val contractDisplayName: String,
    val upcomingRenewal: HomeQuery.UpcomingRenewal,
  ) : HomeModel

  sealed class BodyText : HomeModel {
    object Pending : BodyText()
    object ActiveInFuture : BodyText()
    object Terminated : BodyText()
    object Switching : BodyText()
  }

  data class ClaimStatus(
    val claimStatusCardsUiState: NonEmptyList<ClaimStatusCardUiState>,
  ) : HomeModel

  data class Space(val height: Dp) : HomeModel

  sealed interface StartClaim : HomeModel {
    @get:StringRes
    val textId: Int

    object FirstClaim : StartClaim {
      override val textId: Int = hedvig.resources.R.string.home_tab_claim_button_text
    }

    object NewClaim : StartClaim {
      override val textId: Int = hedvig.resources.R.string.home_open_claim_start_new_claim_button
    }
  }

  data class ConnectPayin(
    val payinType: PaymentType,
  ) : HomeModel

  data class PSA(val inner: HomeQuery.ImportantMessage) : HomeModel

  data class HowClaimsWork(val pages: List<HomeQuery.HowClaimsWork>) : HomeModel

  data class CommonClaims(
    val claims: NonEmptyList<CommonClaim>,
  ) : HomeModel

  data class Header(val stringRes: Int) : HomeModel

  data class PendingAddressChange(val address: String) : HomeModel

  object ChangeAddress : HomeModel
}

sealed interface CommonClaim {

  val title: String

  data class Emergency(
    val inner: EmergencyData,
  ) : CommonClaim {
    override val title: String
      get() = "Emergency"
  }

  data class TitleAndBulletPoints(
    val inner: CommonClaimsData,
  ) : CommonClaim {
    override val title: String
      get() = inner.title
  }

  data object GenerateTravelCertificate : CommonClaim {
    override val title: String
      get() = "Travel Certificate"
  }

  data object ChangeAddress : CommonClaim {
    override val title: String
      get() = "Change address"
  }

  data object Chat : CommonClaim {
    override val title: String
      get() = "Chat"
  }
}
