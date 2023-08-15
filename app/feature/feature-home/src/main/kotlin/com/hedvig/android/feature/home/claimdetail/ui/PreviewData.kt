package com.hedvig.android.feature.home.claimdetail.ui

import com.hedvig.android.feature.home.claimdetail.model.ClaimDetailCardUiState
import com.hedvig.android.feature.home.claimdetail.model.ClaimDetailResult
import com.hedvig.android.feature.home.claimdetail.model.ClaimDetailUiState
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState
import com.hedvig.android.feature.home.claimstatus.data.PillUiState
import giraffe.type.ClaimStatus
import kotlinx.datetime.Clock
import org.javamoney.moneta.CurrencyUnitBuilder
import org.javamoney.moneta.Money
import javax.money.MonetaryAmount
import kotlin.time.Duration.Companion.minutes

internal fun PillUiState.Companion.previewList(): List<PillUiState> {
  return PillUiState.PillType.entries.dropLast(1).map { pillType ->
    PillUiState(pillType.name, pillType)
  }
}

internal fun ClaimProgressUiState.Companion.previewList(): List<ClaimProgressUiState> {
  return ClaimProgressUiState.ClaimProgressType.entries.dropLast(1).map { progressType ->
    ClaimProgressUiState(progressType.name, progressType)
  }
}

internal fun ClaimDetailUiState.Companion.previewData(): ClaimDetailUiState {
  return ClaimDetailUiState(
    claimType = "All-risk",
    insuranceType = "Contents Insurance",
    claimDetailResult = ClaimDetailResult.Closed.Paid(PreviewData.monetaryAmount(2_500)),
    submittedAt = Clock.System.now().minus(30.minutes),
    closedAt = null,
    claimDetailCard = ClaimDetailCardUiState(
      ClaimProgressUiState.previewList(),
      statusParagraph = """
                |Your claim in being reviewed by one of our insurance specialists.
                | We'll get back to you soon with an update.
      """.trimMargin(),
    ),
    signedAudioURL = null,
    claimStatus = ClaimStatus.BEING_HANDLED,
  )
}

/**
 * For classes we do not own, we can not add a Companion object on them to fake a static function of the class itself.
 * This object should serve as a workaround until(if) this is resolved https://youtrack.jetbrains.com/issue/KT-11968.
 */
internal object PreviewData {
  fun monetaryAmount(
    money: Number = 0,
    currencyText: String = "SEK",
  ): MonetaryAmount {
    return Money.of(
      money,
      CurrencyUnitBuilder.of(
        currencyText,
        "default",
      ).build(),
    )
  }
}
