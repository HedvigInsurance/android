package com.hedvig.android.feature.inapppurchase.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseForm
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseIneligibilityReason
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseMocks
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseProduct
import com.hedvig.android.feature.inapppurchase.ui.form.InAppPurchaseFormUiState.Content
import com.hedvig.android.feature.inapppurchase.ui.form.InAppPurchaseFormUiState.Failure
import com.hedvig.android.feature.inapppurchase.ui.form.InAppPurchaseFormUiState.Loading
import com.hedvig.android.feature.inapppurchase.ui.form.InAppPurchaseFormUiState.NotEligible
import hedvig.resources.GENERAL_ERROR_BODY
import hedvig.resources.GENERAL_RETRY
import hedvig.resources.REFERRAL_ADDCOUPON_INPUTPLACEHOLDER
import hedvig.resources.Res
import hedvig.resources.general_close_button
import hedvig.resources.something_went_wrong
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InAppPurchaseFormDestination(
  viewModel: InAppPurchaseFormViewModel,
  navigateUp: () -> Unit,
  popBackstack: () -> Unit,
  onLearnMore: (InAppPurchaseProduct) -> Unit,
  navigateToContract: (contractId: String) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  InAppPurchaseFormScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackstack = dropUnlessResumed { popBackstack() },
    onLearnMore = dropUnlessResumed { (uiState as? Content)?.product?.let(onLearnMore) },
    navigateToContract = { contractId -> navigateToContract(contractId) },
    onReload = { viewModel.emit(InAppPurchaseFormEvent.Reload) },
    onValueChange = { fieldId, value -> viewModel.emit(InAppPurchaseFormEvent.UpdateField(fieldId, value)) },
    onCampaignCodeChange = { code -> viewModel.emit(InAppPurchaseFormEvent.UpdateCampaignCode(code)) },
    onSubmit = { viewModel.emit(InAppPurchaseFormEvent.Submit) },
    dismissQuoteError = { viewModel.emit(InAppPurchaseFormEvent.DismissQuoteError) },
  )
}

@Composable
private fun InAppPurchaseFormScreen(
  uiState: InAppPurchaseFormUiState,
  navigateUp: () -> Unit,
  popBackstack: () -> Unit,
  onLearnMore: () -> Unit,
  navigateToContract: (contractId: String) -> Unit,
  onReload: () -> Unit,
  onValueChange: (fieldId: String, value: String?) -> Unit,
  onCampaignCodeChange: (String) -> Unit,
  onSubmit: () -> Unit,
  dismissQuoteError: () -> Unit,
) {
  when (uiState) {
    Loading -> HedvigFullScreenCenterAlignedProgress(Modifier.fillMaxSize())

    Failure -> HedvigScaffold(navigateUp) {
      Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
        HedvigErrorSection(onButtonClick = onReload)
      }
    }

    is NotEligible -> NotEligibleScreen(uiState, navigateUp, popBackstack, navigateToContract)

    is Content -> FormScreen(
      uiState,
      navigateUp,
      onLearnMore,
      onValueChange,
      onCampaignCodeChange,
      onSubmit,
      dismissQuoteError,
    )
  }
}

@Composable
private fun FormScreen(
  content: Content,
  navigateUp: () -> Unit,
  onLearnMore: () -> Unit,
  onValueChange: (fieldId: String, value: String?) -> Unit,
  onCampaignCodeChange: (String) -> Unit,
  onSubmit: () -> Unit,
  dismissQuoteError: () -> Unit,
) {
  if (content.quoteError != null) {
    ErrorDialog(
      title = stringResource(Res.string.something_went_wrong),
      message = content.quoteError.message ?: stringResource(Res.string.GENERAL_ERROR_BODY),
      buttonText = stringResource(Res.string.GENERAL_RETRY),
      onButtonClick = dismissQuoteError,
      onDismiss = dismissQuoteError,
    )
  }
  HedvigScaffold(navigateUp = navigateUp) {
    ProductHeader(content.product, Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      for (field in content.fields) {
        FormFieldInput(
          state = field,
          enabled = !content.isQuoting,
          onValueChange = { onValueChange(field.field.id, it) },
          modifier = Modifier.fillMaxWidth(),
        )
      }
      val campaignCodeState = rememberTextFieldState(content.campaignCode)
      LaunchedEffect(campaignCodeState) {
        snapshotFlow { campaignCodeState.text.toString() }
          .drop(1)
          .collect { onCampaignCodeChange(it) }
      }
      HedvigTextField(
        state = campaignCodeState,
        labelText = stringResource(Res.string.REFERRAL_ADDCOUPON_INPUTPLACEHOLDER),
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        enabled = !content.isQuoting,
        errorState = if (content.campaignCodeError != null) {
          HedvigTextFieldDefaults.ErrorState.Error.WithMessage(content.campaignCodeError)
        } else {
          HedvigTextFieldDefaults.ErrorState.NoError
        },
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Characters,
          imeAction = ImeAction.Done,
        ),
        lineLimits = TextFieldLineLimits.SingleLine,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      // TODO: Add "Get price" / "Se pris" to Lokalise
      text = "Get price",
      onClick = onSubmit,
      enabled = content.canSubmit,
      isLoading = content.isQuoting,
      buttonSize = Large,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      // TODO: Add "Read about details" / "Läs om detaljerna" to Lokalise
      text = "Read about details",
      onClick = onLearnMore,
      enabled = true,
      buttonStyle = Secondary,
      buttonSize = Large,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun NotEligibleScreen(
  uiState: NotEligible,
  navigateUp: () -> Unit,
  popBackstack: () -> Unit,
  navigateToContract: (contractId: String) -> Unit,
) {
  HedvigScaffold(navigateUp = navigateUp) {
    ProductHeader(uiState.product, Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigNotificationCard(
      message = when (uiState.reason) {
        // TODO: Add "You already have this insurance with us." / "Du har redan den här försäkringen hos oss." to Lokalise
        InAppPurchaseIneligibilityReason.ALREADY_INSURED -> "You already have this insurance with us."

        // TODO: Add "This insurance isn't available to you right now." / "Den här försäkringen är inte tillgänglig för dig just nu." to Lokalise
        InAppPurchaseIneligibilityReason.NOT_AVAILABLE -> "This insurance isn't available to you right now."
      },
      priority = NotificationDefaults.NotificationPriority.Info,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    if (uiState.reason == InAppPurchaseIneligibilityReason.ALREADY_INSURED && uiState.existingContractId != null) {
      HedvigButton(
        // TODO: Add "View your insurance" / "Visa din försäkring" to Lokalise
        text = "View your insurance",
        onClick = dropUnlessResumed { navigateToContract(uiState.existingContractId) },
        enabled = true,
        buttonSize = Large,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
    }
    HedvigButton(
      text = stringResource(Res.string.general_close_button),
      onClick = popBackstack,
      enabled = true,
      buttonStyle = Secondary,
      buttonSize = Large,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun ProductHeader(product: InAppPurchaseProduct, modifier: Modifier = Modifier) {
  FlowHeading(title = product.displayNameFull, description = product.tagline, modifier = modifier)
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewInAppPurchaseFormScreen(
  @PreviewParameter(InAppPurchaseFormUiStateProvider::class) uiState: InAppPurchaseFormUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InAppPurchaseFormScreen(uiState, {}, {}, {}, {}, {}, { _, _ -> }, {}, {}, {})
    }
  }
}

private class InAppPurchaseFormUiStateProvider : CollectionPreviewParameterProvider<InAppPurchaseFormUiState>(
  listOf(
    previewContent(InAppPurchaseMocks.accidentForm),
    previewContent(InAppPurchaseMocks.paymentProtectionForm).let { content ->
      content.copy(
        fields = content.fields.map { field ->
          if (field.field.id == "employmentType") field.copy(value = "UNEMPLOYED") else field
        },
      )
    },
    previewContent(InAppPurchaseMocks.paymentProtectionForm).let { content ->
      content.copy(
        fields = content.fields.map { field ->
          if (field.field.id == "employmentType") field.copy(value = "PERMANENT_EMPLOYED_FULLTIME") else field
        },
      )
    },
    NotEligible(
      product = InAppPurchaseMocks.accidentAlreadyInsuredForm.product,
      reason = InAppPurchaseIneligibilityReason.ALREADY_INSURED,
      existingContractId = "6f1c2a9e-3b7d-4c51-9a0e-2d8b5f4e7c13",
    ),
    NotEligible(
      product = InAppPurchaseMocks.paymentProtectionNotAvailableForm.product,
      reason = InAppPurchaseIneligibilityReason.NOT_AVAILABLE,
      existingContractId = null,
    ),
    Failure,
  ),
)

private fun previewContent(form: InAppPurchaseForm) = Content(
  product = form.product,
  fields = form.fields.map { FormFieldUiState(field = it, value = it.defaultValueOrNull(), errorMessage = null) },
  campaignCode = "",
  campaignCodeError = null,
  isQuoting = false,
  quoteError = null,
)
