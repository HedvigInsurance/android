package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TerminationSummary(
  selectedDate: LocalDate?,
  insuranceDisplayName: String,
  exposureName: String,
  insuranceCardPainter: Painter,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    val chips = if (selectedDate != null) {
      val terminationDateText = terminationDateText(terminationDate = selectedDate)
      persistentListOf(terminationDateText)
    } else {
      persistentListOf()
    }
    InsuranceCard(
      chips = chips,
      topText = insuranceDisplayName,
      bottomText = exposureName,
      imageLoader = imageLoader,
      fallbackPainter = insuranceCardPainter,
    )
    Spacer(Modifier.height(16.dp))
    Text(
      stringResource(id = R.string.TERMINATE_CONTRACT_COMMON_QUESTIONS),
      modifier = Modifier.padding(horizontal = 2.dp),
    )
    Spacer(Modifier.height(16.dp))

    var expandedQuestionId by rememberSaveable { mutableStateOf<Int?>(null) }

    CommonQuestion(
      stringResource(id = R.string.TERMINATION_Q_01),
      stringResource(id = R.string.TERMINATION_A_01),
      isExpanded = expandedQuestionId == 1,
      onClick = {
        expandedQuestionId = if (expandedQuestionId == 1) {
          null
        } else {
          1
        }
      },
    )
    Spacer(Modifier.height(4.dp))
    CommonQuestion(
      stringResource(id = R.string.TERMINATION_Q_02),
      stringResource(id = R.string.TERMINATION_A_02),
      isExpanded = expandedQuestionId == 2,
      onClick = {
        expandedQuestionId = if (expandedQuestionId == 2) {
          null
        } else {
          2
        }
      },
    )
    Spacer(Modifier.height(4.dp))
    CommonQuestion(
      stringResource(id = R.string.TERMINATION_Q_03),
      stringResource(id = R.string.TERMINATION_A_03),
      isExpanded = expandedQuestionId == 3,
      onClick = {
        expandedQuestionId = if (expandedQuestionId == 3) {
          null
        } else {
          3
        }
      },
    )
  }
}

@Composable
private fun terminationDateText(terminationDate: LocalDate): String {
  val formatter = rememberHedvigDateTimeFormatter()
  val formattedDate = formatter.format(terminationDate.toJavaLocalDate())
  return stringResource(R.string.CONTRACT_STATUS_TO_BE_TERMINATED, formattedDate)
}

@HedvigPreview
@Composable
private fun PreviewTerminationSummary() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationSummary(
        Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
        "Insurance display name",
        "exposure name",
        ColorPainter(Color.Magenta),
        rememberPreviewImageLoader(),
      )
    }
  }
}
