package com.hedvig.android.feature.change.mileage.ui.result

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import hedvig.resources.GENERAL_RETRY
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_COMMIT_PROCESSING_ERROR_DESCRIPTION
import hedvig.resources.TIER_FLOW_COMMIT_PROCESSING_TITLE
import hedvig.resources.general_close_button
import hedvig.resources.something_went_wrong
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChangeMileageSuccessScreen(
  newMileageDisplayName: String,
  newMonthlyCost: UiMoney,
  activationDate: LocalDate,
  popBackstack: () -> Unit,
) {
  val price = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, newMonthlyCost)
  val date = rememberHedvigDateTimeFormatter().format(activationDate)
  ResultScreenLayout(onClose = popBackstack) {
    EmptyState(
      text = stringResource(Res.string.TIER_FLOW_COMMIT_PROCESSING_TITLE),
      // TODO: Add "Your mileage will be %1$s per year from %2$s, and your new price is %3$s." / "Din körsträcka blir %1$s per år från %2$s och ditt nya pris är %3$s." to Lokalise
      description = "Din körsträcka blir $newMileageDisplayName per år från $date och ditt nya pris är $price.",
      iconStyle = SUCCESS,
      buttonStyle = NoButton,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
internal fun ChangeMileageFailureScreen(popBackstack: () -> Unit) {
  ResultScreenLayout(onClose = popBackstack) {
    EmptyState(
      text = stringResource(Res.string.something_went_wrong),
      description = stringResource(Res.string.TIER_FLOW_COMMIT_PROCESSING_ERROR_DESCRIPTION),
      iconStyle = ERROR,
      buttonStyle = EmptyStateButtonStyle.Button(
        buttonText = stringResource(Res.string.GENERAL_RETRY),
        onButtonClick = dropUnlessResumed { popBackstack() },
      ),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun ResultScreenLayout(onClose: () -> Unit, content: @Composable () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
  ) {
    Spacer(Modifier.weight(1f))
    content()
    Spacer(Modifier.weight(1f))
    HedvigTextButton(
      text = stringResource(Res.string.general_close_button),
      onClick = dropUnlessResumed { onClose() },
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewChangeMileageSuccessScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChangeMileageSuccessScreen("1 000 mil", UiMoney(371.0, UiCurrencyCode.SEK), LocalDate(2026, 11, 19), {})
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChangeMileageFailureScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChangeMileageFailureScreen({})
    }
  }
}
