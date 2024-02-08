package com.hedvig.android.feature.terminateinsurance.step.terminationsuccess

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import coil.ImageLoader
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenContent
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenScaffold
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TerminationSuccessDestination(
  terminationDate: LocalDate?,
  insuranceDisplayName: String,
  exposureName: String,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
  onSurveyClicked: () -> Unit,
) {
  TerminationOverviewScreenScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.TERMINATE_CONTRACT_CONFIRMATION_TITLE),
  ) {
    TerminationOverviewScreenContent(
      terminationDate = terminationDate,
      insuranceDisplayName = insuranceDisplayName,
      exposureName = exposureName,
      insuranceCardPainter = ColorPainter(MaterialTheme.colorScheme.onSurfaceVariant),
      imageLoader = imageLoader,
      infoText = terminationDate?.let {
        stringResource(
          R.string.TERMINATE_CONTRACT_CONFIRMATION_INFO_TEXT,
          rememberHedvigDateTimeFormatter().format(terminationDate.toJavaLocalDate()),
        )
      },
      containedButtonText = null,
      onContainedButtonClick = null,
      textButtonText = stringResource(R.string.TERMINATION_OPEN_SURVEY_LABEL),
      onTextButtonClick = onSurveyClicked,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationSuccessScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withTerminationDate: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationSuccessDestination(
        terminationDate = if (withTerminationDate) {
          LocalDate.fromEpochDays(300)
        } else {
          null
        },
        insuranceDisplayName = "Test",
        exposureName = "123",
        navigateUp = {},
        imageLoader = rememberPreviewImageLoader(),
        onSurveyClicked = {},
      )
    }
  }
}
