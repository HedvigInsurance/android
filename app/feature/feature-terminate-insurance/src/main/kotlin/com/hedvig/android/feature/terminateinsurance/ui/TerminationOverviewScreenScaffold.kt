package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBar
import com.hedvig.android.core.ui.appbar.m3.TopAppBarActionType
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import hedvig.resources.R
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TerminationOverviewScreenScaffold(
  navigateUp: () -> Unit,
  topAppBarText: String,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
    Column {
      val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBar(
        title = topAppBarText,
        onClick = navigateUp,
        actionType = TopAppBarActionType.BACK,
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        scrollBehavior = scrollBehavior,
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .nestedScroll(scrollBehavior.nestedScrollConnection)
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        content()
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
    }
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = topAppBarText,
    modifier = modifier,
  ) {
    content()
  }
}

/**
 * Common screen content composable for termination flow screens that show the overview + common questions.
 */
@Composable
internal fun ColumnScope.TerminationOverviewScreenContent(
  terminationDate: LocalDate?,
  insuranceDisplayName: String,
  exposureName: String,
  insuranceCardPainter: Painter,
  imageLoader: ImageLoader,
  infoText: String?,
  containedButtonText: String?,
  onContainedButtonClick: (() -> Unit)?,
  textButtonText: String,
  onTextButtonClick: () -> Unit,
  containedButtonColor: Color = MaterialTheme.colorScheme.error,
  onContainedButtonColor: Color = MaterialTheme.colorScheme.onError,
  isContainedButtonLoading: Boolean = false,
) {
  Spacer(Modifier.height(8.dp))
  InsuranceCard(
    chips = buildList {
      if (terminationDate != null) {
        add(terminationDateText(terminationDate = terminationDate))
      }
    }.toPersistentList(),
    topText = insuranceDisplayName,
    bottomText = exposureName,
    imageLoader = imageLoader,
    fallbackPainter = insuranceCardPainter,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  if (infoText != null) {
    Spacer(Modifier.height(16.dp))
    VectorInfoCard(
      text = infoText,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
  }
  Spacer(Modifier.height(16.dp))
  Text(
    stringResource(id = R.string.TERMINATE_CONTRACT_COMMON_QUESTIONS),
    modifier = Modifier.padding(horizontal = 18.dp),
  )
  Spacer(Modifier.height(16.dp))
  CommonQuestions(modifier = Modifier.padding(horizontal = 16.dp))
  Spacer(Modifier.height(32.dp))
  Spacer(Modifier.weight(1f))
  if (containedButtonText != null && onContainedButtonClick != null) {
    HedvigContainedButton(
      text = containedButtonText,
      colors = ButtonDefaults.buttonColors(
        containerColor = containedButtonColor,
        contentColor = onContainedButtonColor,
      ),
      onClick = onContainedButtonClick,
      isLoading = isContainedButtonLoading,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
  }
  HedvigTextButton(
    text = textButtonText,
    onClick = onTextButtonClick,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
}

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
      TerminationOverviewScreenScaffold(
        navigateUp = {},
        topAppBarText = "topAppBarText",
      ) {
        TerminationOverviewScreenContent(
          terminationDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          insuranceDisplayName = "insuranceDisplayName",
          exposureName = "exposureName",
          insuranceCardPainter = ColorPainter(Color.Gray),
          imageLoader = rememberPreviewImageLoader(),
          infoText = "infoText",
          containedButtonText = "containedButtonText",
          onContainedButtonClick = {},
          textButtonText = "textButtonText",
          onTextButtonClick = {},
          containedButtonColor = MaterialTheme.colorScheme.error,
          onContainedButtonColor = MaterialTheme.colorScheme.onError,
        )
      }
    }
  }
}
