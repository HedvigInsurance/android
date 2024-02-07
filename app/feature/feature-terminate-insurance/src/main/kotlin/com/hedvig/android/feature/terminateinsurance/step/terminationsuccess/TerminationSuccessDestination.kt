package com.hedvig.android.feature.terminateinsurance.step.terminationsuccess

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithClose
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.feature.terminateinsurance.ui.TerminationSummary
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun TerminationSuccessDestination(
  selectedDate: LocalDate?,
  insuranceDisplayName: String,
  exposureName: String,
  finish: () -> Unit,
  imageLoader: ImageLoader,
  onSurveyClicked: () -> Unit,
) {
  val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

  Column(
    Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
  ) {
    TopAppBarWithClose(
      onClick = finish,
      title = stringResource(R.string.TERMINATE_CONTRACT_CONFIRMATION_TITLE),
      scrollBehavior = topAppBarScrollBehavior,
    )
    Column(
      Modifier
        .fillMaxSize()
        .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp)
        .padding(top = 8.dp, bottom = 32.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      TerminationSummary(
        selectedDate = selectedDate,
        insuranceDisplayName = insuranceDisplayName,
        exposureName = exposureName,
        insuranceCardPainter = ColorPainter(Color.Black.copy(alpha = 0.7f)),
        imageLoader = imageLoader,
      )

      Spacer(Modifier.height(32.dp))
      Spacer(Modifier.weight(1f))
      HedvigTextButton(
        text = stringResource(id = R.string.TERMINATION_OPEN_SURVEY_LABEL),
        onClick = onSurveyClicked,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationSuccessScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationSuccessDestination(
        selectedDate = LocalDate.fromEpochDays(300),
        insuranceDisplayName = "Test",
        exposureName = "123",
        finish = {},
        imageLoader = rememberPreviewImageLoader(),
        onSurveyClicked = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationSuccessScreenWithoutTerminationDate() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationSuccessDestination(
        selectedDate = LocalDate.fromEpochDays(300),
        insuranceDisplayName = "Test",
        exposureName = "123",
        finish = {},
        imageLoader = rememberPreviewImageLoader(),
        onSurveyClicked = {},
      )
    }
  }
}
