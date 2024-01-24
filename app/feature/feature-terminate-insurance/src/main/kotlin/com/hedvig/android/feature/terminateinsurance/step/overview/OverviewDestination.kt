package com.hedvig.android.feature.terminateinsurance.step.overview

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.card.ExpandablePlusCard
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.android.toDrawableRes
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun OverviewDestination(
  viewModel: OverviewViewModel,
  imageLoader: ImageLoader,
  navigateToNextStep: (TerminateInsuranceStep) -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep == null) return@LaunchedEffect
    navigateToNextStep(nextStep)
  }
  OverviewScreen(
    uiState = uiState,
    onConfirm = {
      viewModel.submitSelectedDate()
    },
    navigateBack = navigateBack,
    imageLoader = imageLoader,
  )
}

@Composable
internal fun OverviewScreen(
  uiState: OverviewUiState,
  onConfirm: () -> Unit,
  navigateBack: () -> Unit,
  imageLoader: ImageLoader,
) {
  Box(
    Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
  ) {
    Column {
      val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBarWithBack(
        onClick = navigateBack,
        title = stringResource(R.string.TERMINATION_CONFIRM_BUTTON),
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
        val terminationDateText = terminationDateText(terminationDate = uiState.selectedDate)
        InsuranceCard(
          chips = persistentListOf(terminationDateText),
          topText = uiState.insuranceDisplayName,
          bottomText = uiState.exposureName,
          imageLoader = imageLoader,
          fallbackPainter = uiState.contractGroup.toDrawableRes()
            .let { drawableRes -> painterResource(id = drawableRes) },
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
        Spacer(Modifier.height(32.dp))
        Spacer(Modifier.weight(1f))
        HedvigContainedButton(
          text = stringResource(id = R.string.TERMINATION_CONFIRM_BUTTON),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
          onClick = onConfirm,
        )
        HedvigTextButton(
          text = stringResource(id = R.string.general_cancel_button),
          onClick = navigateBack,
        )
      }
    }
  }
}

@Composable
private fun CommonQuestion(question: String, answer: String, isExpanded: Boolean, onClick: () -> Unit) {
  ExpandablePlusCard(
    isExpanded = isExpanded,
    onClick = onClick,
    content = {
      Text(
        text = question,
        modifier = Modifier.weight(1f, true),
      )
    },
    expandedContent = {
      RichText(modifier = Modifier.padding(horizontal = 12.dp)) {
        Markdown(content = answer)
      }
    },
  )
}

@Composable
private fun terminationDateText(terminationDate: LocalDate): String {
  val formatter = rememberHedvigDateTimeFormatter()
  val formattedDate = formatter.format(terminationDate.toJavaLocalDate())
  return "Terminates on $formattedDate"
}

@HedvigPreview
@Composable
internal fun OverviewScreenPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      OverviewScreen(
        uiState = OverviewUiState(
          selectedDate = LocalDate.fromEpochDays(300),
          insuranceDisplayName = "Test insurance",
          nextStep = null,
          errorMessage = null,
          isLoading = false,
          exposureName = "destination.exposureName",
          contractGroup = ContractGroup.CAR,
        ),
        imageLoader = rememberPreviewImageLoader(),
        onConfirm = {},
        navigateBack = {},
      )
    }
  }
}
