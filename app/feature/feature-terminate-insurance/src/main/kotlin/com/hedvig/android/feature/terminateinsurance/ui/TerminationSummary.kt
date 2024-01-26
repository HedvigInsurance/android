package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TerminationSummary(
  selectedDate: LocalDate?,
  insuranceDisplayName: String,
  exposureName: String,
  painter: Painter,
  imageLoader: ImageLoader,
) {
  Column {
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
      fallbackPainter = painter,
    )
    Spacer(Modifier.height(16.dp))
    Text(stringResource(id = R.string.TERMINATE_CONTRACT_COMMON_QUESTIONS))
    Spacer(Modifier.height(16.dp))

    var expandedQuestionId by remember { mutableStateOf<Int?>(null) }

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
  return "Terminates on $formattedDate"
}
