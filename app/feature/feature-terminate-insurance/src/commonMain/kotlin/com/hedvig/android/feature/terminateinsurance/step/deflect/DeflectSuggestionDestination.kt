package com.hedvig.android.feature.terminateinsurance.step.deflect

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.terminateinsurance.data.SuggestionType
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.GENERAL_CONTACT_US_TITLE
import hedvig.resources.Res
import hedvig.resources.TERMINATION_BUTTON
import hedvig.resources.TERMINATION_FLOW_AUTO_CANCEL_ABOUT
import hedvig.resources.TERMINATION_FLOW_AUTO_CANCEL_DECOM
import hedvig.resources.TERMINATION_FLOW_AUTO_CANCEL_RECOMMISSION
import hedvig.resources.TERMINATION_FLOW_AUTO_CANCEL_SCRAPPED
import hedvig.resources.TERMINATION_FLOW_AUTO_CANCEL_SOLD
import hedvig.resources.TERMINATION_FLOW_AUTO_CANCEL_TITLE
import hedvig.resources.TERMINATION_FLOW_AUTO_DECOM_COSTS_INFO
import hedvig.resources.TERMINATION_FLOW_AUTO_DECOM_COSTS_TITLE
import hedvig.resources.TERMINATION_FLOW_AUTO_DECOM_COVERED_INFO
import hedvig.resources.TERMINATION_FLOW_AUTO_DECOM_COVERED_TITLE
import hedvig.resources.TERMINATION_FLOW_AUTO_DECOM_INFO
import hedvig.resources.TERMINATION_FLOW_AUTO_DECOM_NOTIFICATION
import hedvig.resources.TERMINATION_FLOW_AUTO_DECOM_TITLE
import hedvig.resources.TERMINATION_FLOW_AUTO_RECOMMISSION_TITLE
import hedvig.resources.TERMINATION_FLOW_I_UNDERSTAND_TEXT
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeflectSuggestionDestination(
  description: String,
  suggestionType: SuggestionType,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onContinueTermination: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  val content = rememberDeflectScreenContent(suggestionType, apiDescription = description)
  DeflectSuggestionScreen(
    content = content,
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    onContinueTermination = onContinueTermination,
    onNavigateToNewConversation = onNavigateToNewConversation,
  )
}

@Composable
private fun DeflectSuggestionScreen(
  content: DeflectScreenContent,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onContinueTermination: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) { scaffoldTitle ->
    FlowHeading(
      title = content.headingTitle ?: scaffoldTitle,
      description = if (content.headingTitle != null) null else content.title,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigText(
      content.message,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (content.extraMessage != null) {
      Spacer(Modifier.height(16.dp))
      HedvigText(
        content.extraMessage,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    for (explanation in content.explanations) {
      Spacer(Modifier.height(16.dp))
      HedvigText(
        explanation.title,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(4.dp))
      HedvigText(
        explanation.text,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.weight(1f).heightIn(min = 16.dp))
    if (content.info != null) {
      Spacer(Modifier.height(16.dp))
      HedvigNotificationCard(
        message = content.info,
        priority = NotificationDefaults.NotificationPriority.Info,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
    }
    HedvigButton(
      text = stringResource(Res.string.TERMINATION_FLOW_I_UNDERSTAND_TEXT),
      enabled = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = closeTerminationFlow,
    )
    if (content.canContinueTermination) {
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.TERMINATION_BUTTON),
        buttonSize = Large,
        onClick = onContinueTermination,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    if (content.showContactUs) {
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.GENERAL_CONTACT_US_TITLE),
        buttonSize = Large,
        onClick = onNavigateToNewConversation,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

private data class DeflectScreenContent(
  val headingTitle: String?,
  val title: String,
  val message: String,
  val extraMessage: String?,
  val explanations: List<ExplanationItem>,
  val info: String?,
  val canContinueTermination: Boolean,
  val showContactUs: Boolean = false,
)

private data class ExplanationItem(
  val title: String,
  val text: String,
)

@Composable
private fun rememberDeflectScreenContent(
  suggestionType: SuggestionType,
  apiDescription: String,
): DeflectScreenContent {
  val autoCancelTitle = stringResource(Res.string.TERMINATION_FLOW_AUTO_CANCEL_TITLE)
  val autoCancelAbout = stringResource(Res.string.TERMINATION_FLOW_AUTO_CANCEL_ABOUT)
  val decommissionMessage = stringResource(Res.string.TERMINATION_FLOW_AUTO_CANCEL_DECOM)
  val soldMessage = stringResource(Res.string.TERMINATION_FLOW_AUTO_CANCEL_SOLD)
  val scrappedMessage = stringResource(Res.string.TERMINATION_FLOW_AUTO_CANCEL_SCRAPPED)
  val decomTitle = stringResource(Res.string.TERMINATION_FLOW_AUTO_DECOM_TITLE)
  val decomInfo = stringResource(Res.string.TERMINATION_FLOW_AUTO_DECOM_INFO)
  val decomCoveredTitle = stringResource(Res.string.TERMINATION_FLOW_AUTO_DECOM_COVERED_TITLE)
  val decomCoveredInfo = stringResource(Res.string.TERMINATION_FLOW_AUTO_DECOM_COVERED_INFO)
  val decomCostsTitle = stringResource(Res.string.TERMINATION_FLOW_AUTO_DECOM_COSTS_TITLE)
  val decomCostsInfo = stringResource(Res.string.TERMINATION_FLOW_AUTO_DECOM_COSTS_INFO)
  val decomNotification = stringResource(Res.string.TERMINATION_FLOW_AUTO_DECOM_NOTIFICATION)
  val carBackTitle = stringResource(Res.string.TERMINATION_FLOW_AUTO_RECOMMISSION_TITLE)
  val carBackMessage = stringResource(Res.string.TERMINATION_FLOW_AUTO_CANCEL_RECOMMISSION)

  return remember(suggestionType, apiDescription) {
    when (suggestionType) {
      SuggestionType.AUTO_CANCEL_SOLD -> autoCancel(
        headingTitle = autoCancelTitle,
        message = soldMessage,
        extraMessage = autoCancelAbout,
        canContinueTermination = false,
        showContactUs = true,
      )

      SuggestionType.AUTO_CANCEL_SCRAPPED -> autoCancel(
        headingTitle = autoCancelTitle,
        message = scrappedMessage,
        extraMessage = autoCancelAbout,
        canContinueTermination = false,
        showContactUs = true,
      )

      SuggestionType.AUTO_CANCEL_DECOMMISSION -> autoCancel(
        headingTitle = autoCancelTitle,
        message = decommissionMessage,
        extraMessage = autoCancelAbout,
        canContinueTermination = true,
        showContactUs = false,
      )

      SuggestionType.AUTO_DECOMMISSION -> DeflectScreenContent(
        headingTitle = decomTitle,
        title = "",
        message = decomInfo,
        extraMessage = null,
        explanations = listOf(
          ExplanationItem(title = decomCoveredTitle, text = decomCoveredInfo),
          ExplanationItem(title = decomCostsTitle, text = decomCostsInfo),
        ),
        info = decomNotification,
        canContinueTermination = true,
      )

      SuggestionType.CAR_ALREADY_DECOMMISSION -> DeflectScreenContent(
        headingTitle = carBackTitle,
        title = "",
        message = carBackMessage,
        extraMessage = null,
        explanations = emptyList(),
        info = null,
        canContinueTermination = true,
      )

      else -> DeflectScreenContent(
        headingTitle = null,
        title = apiDescription,
        message = "",
        extraMessage = null,
        explanations = emptyList(),
        info = null,
        canContinueTermination = false,
      )
    }
  }
}

private fun autoCancel(
  headingTitle: String,
  message: String,
  extraMessage: String,
  canContinueTermination: Boolean,
  showContactUs: Boolean,
): DeflectScreenContent {
  return DeflectScreenContent(
    headingTitle = headingTitle,
    title = "",
    message = message,
    extraMessage = extraMessage,
    explanations = emptyList(),
    info = null,
    canContinueTermination = canContinueTermination,
    showContactUs = showContactUs,
  )
}

@HedvigPreview
@Composable
private fun PreviewDeflectAutoCancel() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectSuggestionScreen(
        content = DeflectScreenContent(
          headingTitle = "We'll cancel your insurance automatically",
          title = "",
          message = "Since you've sold your car, your insurance will be automatically cancelled.",
          extraMessage = "We'll send a cancellation confirmation within a few days.",
          explanations = emptyList(),
          info = null,
          canContinueTermination = false,
          showContactUs = true,
        ),
        navigateUp = {},
        closeTerminationFlow = {},
        onContinueTermination = {},
        onNavigateToNewConversation = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDeflectAutoDecom() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectSuggestionScreen(
        content = DeflectScreenContent(
          headingTitle = "Your insurance will switch to decommission insurance",
          title = "",
          message = "If you've decommissioned your car, your insurance will automatically switch.",
          extraMessage = null,
          explanations = listOf(
            ExplanationItem("What's covered", "Theft, fire, vandalism, and body damage."),
            ExplanationItem("What it costs", "You'll receive a confirmation email."),
          ),
          info = "If you don't want to keep your decommission insurance, you can cancel it below.",
          canContinueTermination = true,
        ),
        navigateUp = {},
        closeTerminationFlow = {},
        onContinueTermination = {},
        onNavigateToNewConversation = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDeflectCarAlreadyDecom() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectSuggestionScreen(
        content = DeflectScreenContent(
          headingTitle = "Your car is back on the road",
          title = "",
          message = "Since your car is registered again, your insurance will switch back.",
          extraMessage = null,
          explanations = emptyList(),
          info = null,
          canContinueTermination = true,
        ),
        navigateUp = {},
        closeTerminationFlow = {},
        onContinueTermination = {},
        onNavigateToNewConversation = {},
      )
    }
  }
}
