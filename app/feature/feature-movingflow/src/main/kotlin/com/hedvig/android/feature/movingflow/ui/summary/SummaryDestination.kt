package com.hedvig.android.feature.movingflow.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDialogError
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveMtaQuote
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError.Generic
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError.WithMessage
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Loading
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.NoMatchingQuoteFound
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun SummaryDestination(
  viewModel: SummaryViewModel,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  SummaryScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
    onNavigateToNewConversation = onNavigateToNewConversation,
    onConfirmChanges = { viewModel.emit(SummaryEvent.ConfirmChanges) },
    onDismissSubmissionError = { viewModel.emit(SummaryEvent.DismissSubmissionError) },
  )
}

@Composable
private fun SummaryScreen(
  uiState: SummaryUiState,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onConfirmChanges: () -> Unit,
  onDismissSubmissionError: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      Loading -> HedvigFullScreenCenterAlignedProgress()
      MissingOngoingMovingFlow -> HedvigErrorSection(
        onButtonClick = navigateBack,
        subTitle = null,
        buttonText = stringResource(R.string.general_back_button),
      )

      NoMatchingQuoteFound -> HedvigErrorSection(
        onButtonClick = navigateBack,
        subTitle = null,
        buttonText = stringResource(R.string.general_back_button),
      )

      is Content -> SummaryScreen(
        content = uiState,
        navigateUp = navigateUp,
        onNavigateToNewConversation = onNavigateToNewConversation,
        onConfirmChanges = onConfirmChanges,
        onDismissSubmissionError = onDismissSubmissionError,
      )
    }
  }
}

@Composable
private fun SummaryScreen(
  content: SummaryUiState.Content,
  navigateUp: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onConfirmChanges: () -> Unit,
  onDismissSubmissionError: () -> Unit,
) {
  if (content.submitError != null) {
    HedvigDialogError(
      titleText = stringResource(R.string.something_went_wrong),
      descriptionText = when (content.submitError) {
        Generic -> stringResource(R.string.GENERAL_ERROR_BODY)
        is WithMessage -> content.submitError.message
      },
      buttonText = stringResource(R.string.general_close_button),
      onButtonClick = onDismissSubmissionError,
      onDismissRequest = onDismissSubmissionError,
    )
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.CHANGE_ADDRESS_SUMMARY_TITLE),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      HomeQuoteCard(content.summaryInfo.moveHomeQuote)
      for (mtaQuote in content.summaryInfo.moveMtaQuotes) {
        MtaQuoteCard(mtaQuote)
      }
    }
    Spacer(Modifier.height(16.dp))
    HedvigNotificationCard(stringResource(R.string.CHANGE_ADDRESS_OTHER_INSURANCES_INFO_TEXT), Info)
    Spacer(Modifier.height(24.dp))
    HedvigText("Total: ${content.summaryInfo.moveHomeQuote.premium} / mo")
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.CHANGE_ADDRESS_ACCEPT_OFFER),
      enabled = true,
      onClick = onConfirmChanges,
    )
    Spacer(Modifier.height(40.dp))
    QuestionsAndAnswers()
    Spacer(Modifier.height(40.dp))
    Column {
      HedvigText(stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_TITLE))
      Spacer(Modifier.height(24.dp))
      HedvigButton(
        text = stringResource(R.string.open_chat),
        enabled = true,
        onClick = onNavigateToNewConversation,
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun HomeQuoteCard(moveHomeQuote: MoveHomeQuote, modifier: Modifier = Modifier) {
  HedvigCard(modifier) {
    HedvigText(
      """  
      id:${moveHomeQuote.id}
      exposureName:${moveHomeQuote.exposureName}
      premium:${moveHomeQuote.premium}
      startDate:${moveHomeQuote.startDate}
      tierName:${moveHomeQuote.tierName}
      tierLevel:${moveHomeQuote.tierLevel}
      deductible:${moveHomeQuote.deductible}
      """.trimIndent(),
    )
  }
}

@Composable
private fun MtaQuoteCard(mtaQuote: MoveMtaQuote, modifier: Modifier = Modifier) {
  HedvigCard(modifier) {
    HedvigText(
      """  
      exposureName:${mtaQuote.exposureName}
      premium:${mtaQuote.premium}
      startDate:${mtaQuote.startDate}
      """.trimIndent(),
    )
  }
}

@Composable
private fun QuestionsAndAnswers(modifier: Modifier = Modifier) {
  val faqs = remember {
    listOf(
      R.string.CHANGE_ADDRESS_FAQ_DATE_TITLE to R.string.CHANGE_ADDRESS_FAQ_DATE_LABEL,
      R.string.CHANGE_ADDRESS_FAQ_PRICE_TITLE to R.string.CHANGE_ADDRESS_FAQ_PRICE_LABEL,
      R.string.CHANGE_ADDRESS_FAQ_RENTBRF_TITLE to R.string.CHANGE_ADDRESS_FAQ_RENTBRF_LABEL,
      R.string.CHANGE_ADDRESS_FAQ_STORAGE_TITLE to R.string.CHANGE_ADDRESS_FAQ_STORAGE_LABEL,
      R.string.CHANGE_ADDRESS_FAQ_STUDENT_TITLE to R.string.CHANGE_ADDRESS_FAQ_STUDENT_LABEL,
    )
  }
  Column(modifier) {
    HedvigText(stringResource(R.string.CHANGE_ADDRESS_QA))
    Spacer(Modifier.height(24.dp))
    AccordionList(
      items = faqs.map { (title, description) ->
        AccordionData(
          title = stringResource(title),
          description = stringResource(description),
        )
      },
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewSummaryScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val productVariant = ProductVariant(
        displayName = "Variant",
        contractGroup = ContractGroup.RENTAL,
        contractType = ContractType.SE_APARTMENT_RENT,
        partner = null,
        perils = listOf(),
        insurableLimits = listOf(),
        documents = listOf(),
      )
      val startDate = LocalDate.parse("2025-01-01")
      SummaryScreen(
        uiState = SummaryUiState.Content(
          summaryInfo = SummaryInfo(
            moveHomeQuote = MoveHomeQuote(
              id = "id",
              premium = UiMoney(99.0, SEK),
              startDate = startDate,
              tierName = "tierName",
              tierLevel = 1,
              deductible = Deductible(UiMoney(1500.0, SEK), null, "displayText"),
              displayItems = emptyList(),
              exposureName = "exposureName",
              productVariant = productVariant,
              defaultChoice = false,
            ),
            moveMtaQuotes = listOf(
              MoveMtaQuote(
                premium = UiMoney(49.0, SEK),
                exposureName = "exposureName",
                productVariant = productVariant,
                startDate = startDate,
                displayItems = emptyList(),
              ),
            ),
          ),
          false,
          null,
          null,
        ),
        navigateUp = {},
        navigateBack = {},
        onNavigateToNewConversation = {},
        onConfirmChanges = {},
        onDismissSubmissionError = {},
      )
    }
  }
}
