package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.scaffold.TopAppBarWithBackAndClose
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TerminationScaffold(
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = modifier.fillMaxSize(),
  ) {
    Box {
      Column {
        val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        TopAppBarWithBackAndClose(
          onNavigateUp = navigateUp,
          onClose = closeTerminationFlow,
          title = "",
          scrollBehavior = topAppBarScrollBehavior,
        )
        Column(
          modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(
              WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
              ),
            ),
        ) {
          Text(
            text = stringResource(id = R.string.TERMINATION_FLOW_CANCELLATION_TITLE), // todo: change copy
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
          content()
        }
      }
    }
  }
}

// will probably need it later
@Composable
private fun CommonQuestions(modifier: Modifier = Modifier) {
  var expandedQuestionId by rememberSaveable<MutableState<Int?>> { mutableStateOf(null) }
  Column(modifier) {
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
private fun PreviewTerminationOverviewScreenScaffold() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationScaffold(
        navigateUp = {},
        closeTerminationFlow = {},
      ) {
      }
    }
  }
}
