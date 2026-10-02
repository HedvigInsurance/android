package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LoadingState
import com.hedvig.android.design.system.hedvig.PaymentMethodHandoverIllustration
import com.hedvig.android.design.system.hedvig.PaymentMethodMarkSize
import com.hedvig.android.design.system.hedvig.PaymentMethodTile
import com.hedvig.android.design.system.hedvig.PaymentMethodTileBadge
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ThreeDotsLoading
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.feature.payin.account.ui.setupswish.SwishPayinStatusUiState.Connected
import com.hedvig.android.feature.payin.account.ui.setupswish.SwishPayinStatusUiState.Failed
import com.hedvig.android.feature.payin.account.ui.setupswish.SwishPayinStatusUiState.Loading
import com.hedvig.android.feature.payin.account.ui.setupswish.SwishPayinStatusUiState.PendingApproval
import hedvig.resources.GENERAL_RETRY
import hedvig.resources.PAYMENT_CHANGE_FOOTNOTE
import hedvig.resources.PAYMENT_CHANGE_METHOD_BUTTON
import hedvig.resources.PAYMENT_OPEN_SWISH_BUTTON
import hedvig.resources.PAYMENT_SWISH_APPROVE_TITLE
import hedvig.resources.PAYMENT_SWISH_EXPLANATION_BUTTON
import hedvig.resources.PAYMENT_SWISH_FAILURE_TITLE
import hedvig.resources.PAYMENT_SWISH_SUCCESS_SUBTITLE
import hedvig.resources.PAYMENT_SWISH_SUCCESS_TITLE
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SwishPayinStatusDestination(
  viewModel: SwishPayinStatusViewModel,
  allowSandboxSwishApp: Boolean,
  showSuccessScreen: Boolean,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  finishSwishSetup: () -> Unit,
  changePaymentMethod: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val swishAppHandover = rememberSwishAppHandover(allowSandboxSwishApp, openUrl)

  val leavesWithoutConfirming = !showSuccessScreen && uiState is Connected
  LaunchedEffect(leavesWithoutConfirming) {
    if (leavesWithoutConfirming) finishSwishSetup()
  }

  if (leavesWithoutConfirming) {
    // The pop is already under way, and the entry stays on screen for the duration of its exit
    // animation, so drawing the connected screen here would flash it on the way out.
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
      modifier = Modifier.fillMaxSize(),
    ) {
      HedvigFullScreenCenterAlignedProgress()
    }
  } else {
    SwishPayinStatusScreen(
      uiState = uiState,
      isSwishInstalled = swishAppHandover.isSwishInstalled,
      navigateUp = navigateUp,
      onCancel = navigateBack,
      onContinue = finishSwishSetup,
      onRetry = { viewModel.emit(SwishPayinStatusEvent.Retry) },
      onChangePaymentMethod = changePaymentMethod,
      openUrl = { url ->
        viewModel.emit(SwishPayinStatusEvent.DidOpenSwishApp)
        swishAppHandover.open(url)
      },
    )
  }
}

@Composable
private fun SwishPayinStatusScreen(
  uiState: SwishPayinStatusUiState,
  isSwishInstalled: Boolean,
  navigateUp: () -> Unit,
  onCancel: () -> Unit,
  onContinue: () -> Unit,
  onRetry: () -> Unit,
  onChangePaymentMethod: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val explanationSheetState = rememberHedvigBottomSheetState<Unit>()
  RecurringSwishExplanationBottomSheet(
    sheetState = explanationSheetState,
    onLearnMore = null,
    // TODO: point this at the recurring-Swish article once we know where it lives.
  )

  HedvigScaffold(
    topAppBarText = null,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    if (uiState == Loading) {
      HedvigFullScreenCenterAlignedProgress(Modifier.weight(1f))
    } else {
      SwishPayinStatusContent(
        uiState = uiState,
        isSwishInstalled = isSwishInstalled,
        onCancel = onCancel,
        onContinue = onContinue,
        onRetry = onRetry,
        onChangePaymentMethod = onChangePaymentMethod,
        onLearnMoreAboutRecurringSwish = { explanationSheetState.show() },
        openUrl = openUrl,
      )
    }
  }
}

@Composable
private fun ColumnScope.SwishPayinStatusContent(
  uiState: SwishPayinStatusUiState,
  isSwishInstalled: Boolean,
  onCancel: () -> Unit,
  onContinue: () -> Unit,
  onRetry: () -> Unit,
  onChangePaymentMethod: () -> Unit,
  onLearnMoreAboutRecurringSwish: () -> Unit,
  openUrl: (String) -> Unit,
) {
  Spacer(Modifier.height(8.dp))
  FlowHeading(
    title = when (uiState) {
      is PendingApproval -> stringResource(Res.string.PAYMENT_SWISH_APPROVE_TITLE)
      Connected -> stringResource(Res.string.PAYMENT_SWISH_SUCCESS_TITLE)
      is Failed -> stringResource(Res.string.PAYMENT_SWISH_FAILURE_TITLE)
      Loading -> ""
    },
    description = when (uiState) {
      is PendingApproval -> null
      Connected -> stringResource(Res.string.PAYMENT_SWISH_SUCCESS_SUBTITLE)
      is Failed -> uiState.message ?: stringResource(Res.string.something_went_wrong)
      Loading -> null
    },
    baseStyle = HedvigTheme.typography.bodySmall,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.weight(1f))
  when (uiState) {
    Loading -> {}

    Connected -> {
      PaymentMethodHandoverIllustration(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        destinationBadge = { SetupStatusBadge(uiState) },
        loadingState = LoadingState.ACTIVE,
        mark = { Image(HedvigIcons.Swish, EmptyContentDescription, Modifier.size(PaymentMethodMarkSize)) },
      )
    }

    is Failed -> {
      PaymentMethodHandoverIllustration(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        destinationBadge = { SetupStatusBadge(uiState) },
        loadingState = LoadingState.INACTIVE,
        mark = { Image(HedvigIcons.Swish, EmptyContentDescription, Modifier.size(PaymentMethodMarkSize)) },
      )
    }

    is PendingApproval if uiState.isHandedOver -> {
      PaymentMethodHandoverIllustration(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        loadingState = LoadingState.PROCESSING,
        mark = { Image(HedvigIcons.Swish, EmptyContentDescription, Modifier.size(PaymentMethodMarkSize)) },
      )
    }

    is PendingApproval -> {
      SwishApprovalQrCode(
        redirectUrl = uiState.redirectUrl,
        modifier = Modifier.align(Alignment.CenterHorizontally),
      )
      if (isSwishInstalled) {
        Spacer(Modifier.height(18.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
          ThreeDotsLoading()
        }
      }
    }
  }

  Spacer(Modifier.weight(1f))
  when (uiState) {
    Loading -> {}

    is PendingApproval -> {
      RecurringSwishExplanationButton(onClick = onLearnMoreAboutRecurringSwish)
      Spacer(Modifier.height(16.dp))
      ChangeMethodFootnote()
      Spacer(Modifier.height(16.dp))
      if (uiState.isHandedOver) {
        // The order's token went to Swish with the member, so a fresh order is the only way to try again.
        HedvigButton(
          text = stringResource(Res.string.GENERAL_RETRY),
          onClick = onRetry,
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
      } else if (isSwishInstalled) {
        HedvigButton(
          text = stringResource(Res.string.PAYMENT_OPEN_SWISH_BUTTON),
          onClick = { openUrl(uiState.redirectUrl) },
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
      }
      HedvigTextButton(
        text = stringResource(Res.string.general_cancel_button),
        onClick = onCancel,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }

    Connected -> {
      ChangeMethodFootnote()
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(Res.string.general_continue_button),
        onClick = onContinue,
        enabled = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }

    is Failed -> {
      HedvigButton(
        text = stringResource(Res.string.GENERAL_RETRY),
        onClick = onRetry,
        enabled = !uiState.isRetrying,
        isLoading = uiState.isRetrying,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.PAYMENT_CHANGE_METHOD_BUTTON),
        onClick = onChangePaymentMethod,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
  }
  Spacer(Modifier.height(16.dp))
}

@Composable
private fun ColumnScope.RecurringSwishExplanationButton(onClick: () -> Unit) {
  HedvigText(
    text = stringResource(Res.string.PAYMENT_SWISH_EXPLANATION_BUTTON),
    style = HedvigTheme.typography.label,
    color = HedvigTheme.colorScheme.textSecondary,
    textAlign = TextAlign.Center,
    textDecoration = TextDecoration.Underline,
    modifier = Modifier
      .align(Alignment.CenterHorizontally)
      .clip(HedvigTheme.shapes.cornerSmall)
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 4.dp),
  )
}

@Composable
private fun ChangeMethodFootnote() {
  HedvigText(
    text = stringResource(Res.string.PAYMENT_CHANGE_FOOTNOTE),
    style = HedvigTheme.typography.label,
    color = HedvigTheme.colorScheme.textSecondaryTranslucent,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}

@Composable
private fun SwishPendingIllustration() {
  Column(
    Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box {
      PaymentMethodTile(
        mark = {
          Image(
            HedvigIcons.Swish,
            EmptyContentDescription,
            Modifier.size(PaymentMethodMarkSize),
          )
        },
      )
      Box(
        Modifier
          .align(Alignment.TopEnd)
          .offset(8.dp, (-8).dp),
      ) {
        PaymentMethodTileBadge(
          icon = HedvigIcons.ArrowNorthEast,
          containerColor = HedvigTheme.colorScheme.signalBlueElement,
          contentColor = HedvigTheme.colorScheme.fillWhite,
        )
      }
    }
    Spacer(Modifier.height(48.dp))
    ThreeDotsLoading()
  }
}

@Composable
private fun SwishApprovalQrCode(redirectUrl: String, modifier: Modifier = Modifier) {
  Surface(
    color = Color.White,
    shape = HedvigTheme.shapes.cornerMedium,
    border = HedvigTheme.colorScheme.borderPrimary,
    modifier = modifier,
  ) {
    SwishQrCode(
      content = redirectUrl,
      modifier = Modifier
        .size(180.dp)
        .padding(16.dp),
    )
  }
}

/** Marks the Hedvig symbol with where the setup has got to: landed, or rejected. */
@Composable
private fun SetupStatusBadge(uiState: SwishPayinStatusUiState) {
  when (uiState) {
    Loading, is PendingApproval -> {}

    Connected -> {
      PaymentMethodTileBadge(
        icon = HedvigIcons.Checkmark,
        containerColor = HedvigTheme.colorScheme.signalGreenElement,
        contentColor = HedvigTheme.colorScheme.fillWhite,
      )
    }

    is Failed -> {
      PaymentMethodTileBadge(
        icon = HedvigIcons.Close,
        containerColor = HedvigTheme.colorScheme.signalAmberElement,
        contentColor = HedvigTheme.colorScheme.fillWhite,
      )
    }
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewSwishPayinStatusScreen(
  @PreviewParameter(SwishPayinStatusUiStateProvider::class) uiState: SwishPayinStatusUiState,
) {
  SwishPayinStatusScreenPreviewContent(uiState, isSwishInstalled = true)
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewSwishPayinStatusScreenWithoutSwishApp() {
  SwishPayinStatusScreenPreviewContent(PendingApproval("https://swish"), isSwishInstalled = false)
}

@Composable
private fun SwishPayinStatusScreenPreviewContent(uiState: SwishPayinStatusUiState, isSwishInstalled: Boolean) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SwishPayinStatusScreen(
        uiState = uiState,
        isSwishInstalled = isSwishInstalled,
        navigateUp = {},
        onCancel = {},
        onContinue = {},
        onRetry = {},
        onChangePaymentMethod = {},
        openUrl = {},
      )
    }
  }
}

private class SwishPayinStatusUiStateProvider : CollectionPreviewParameterProvider<SwishPayinStatusUiState>(
  listOf(
    Loading,
    PendingApproval("https://swish"),
    PendingApproval("https://swish", isHandedOver = true),
    Connected,
    Failed(null),
    Failed("The connection was declined in the Swish app", isRetrying = true),
  ),
)
