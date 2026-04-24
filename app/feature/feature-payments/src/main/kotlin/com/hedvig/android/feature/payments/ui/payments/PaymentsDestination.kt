package com.hedvig.android.feature.payments.ui.payments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigInformationSection
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Button
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.Campaign
import com.hedvig.android.design.system.hedvig.icon.Card
import com.hedvig.android.design.system.hedvig.icon.ChevronRight
import com.hedvig.android.design.system.hedvig.icon.Clock
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.design.system.hedvig.placeholder.shimmer
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.feature.payments.data.ManualChargeToPrompt
import com.hedvig.android.feature.payments.data.PaymentOverview.OngoingCharge
import com.hedvig.android.feature.payments.ui.payments.PaymentsEvent.Retry
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.ConnectedPaymentInfo
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPayment
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPayment.NoUpcomingPayment
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPaymentInfo
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPaymentInfo.InProgress
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPaymentInfo.NoInfo
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPaymentInfo.PaymentFailed
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Error
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Loading
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import hedvig.resources.MY_PAYMENT_UPDATING_MESSAGE
import hedvig.resources.PAYMENTS_DISCOUNTS_SECTION_TITLE
import hedvig.resources.PAYMENTS_IN_PROGRESS
import hedvig.resources.PAYMENTS_MISSED_PAYMENT
import hedvig.resources.PAYMENTS_NO_PAYMENTS_IN_PROGRESS
import hedvig.resources.PAYMENTS_PAYMENT_DETAILS_INFO_TITLE
import hedvig.resources.PAYMENTS_PAYMENT_HISTORY_BUTTON_LABEL
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_AMOUNT_DUE
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_BODY
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_BUTTON
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_TITLE
import hedvig.resources.PAYMENTS_PROCESSING_PAYMENT
import hedvig.resources.PAYMENTS_UPCOMING_PAYMENT
import hedvig.resources.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_TITLE
import hedvig.resources.R
import hedvig.resources.Res
import hedvig.resources.TAB_PAYMENTS_TITLE
import hedvig.resources.info_card_missing_payment_body
import hedvig.resources.info_card_missing_payment_missing_payments_body
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaymentsDestination(
  viewModel: PaymentsViewModel,
  onPaymentClicked: (id: String?) -> Unit,
  onDiscountClicked: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onMemberPaymentDetailsClicked: () -> Unit,
  onChangeBankAccount: () -> Unit,
  onOpenManualCharge: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PaymentsScreen(
    uiState = uiState,
    onUpcomingPaymentClicked = onPaymentClicked,
    onChangeBankAccount = onChangeBankAccount,
    onDiscountClicked = onDiscountClicked,
    onPaymentHistoryClicked = onPaymentHistoryClicked,
    onRetry = { viewModel.emit(Retry) },
    onPaymentDetailsClicked = onMemberPaymentDetailsClicked,
    onOpenManualCharge = onOpenManualCharge,
  )
}

@Composable
private fun PaymentsScreen(
  uiState: PaymentsUiState,
  onUpcomingPaymentClicked: (memberChargeId: String?) -> Unit,
  onChangeBankAccount: () -> Unit,
  onDiscountClicked: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onPaymentDetailsClicked: () -> Unit,
  onOpenManualCharge: () -> Unit,
  onRetry: () -> Unit,
) {
  val density = LocalDensity.current
  val systemBarInsetTopDp = with(density) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val isRefreshing =
    uiState is Loading || uiState.safeCast<Content>()?.isRetrying == true
  val pullRefreshState = rememberPullRefreshState(
    refreshing = isRefreshing,
    onRefresh = onRetry,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  Box(
    modifier = Modifier
      .fillMaxSize()
      .pullRefresh(pullRefreshState),
  ) {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics(mergeDescendants = true) {
              heading()
            },
        ) {
          HedvigText(
            text = stringResource(Res.string.TAB_PAYMENTS_TITLE),
            style = HedvigTheme.typography.headlineSmall,
          )
        }
        when (uiState) {
          Error -> {
            HedvigErrorSection(
              onButtonClick = onRetry,
              Modifier.weight(1f),
              windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            )
          }

          else -> {
            PaymentsContent(
              uiState = uiState,
              onUpcomingPaymentClicked = { id ->
                onUpcomingPaymentClicked(id)
              },
              onChangeBankAccount = onChangeBankAccount,
              onDiscountClicked = onDiscountClicked,
              onPaymentHistoryClicked = onPaymentHistoryClicked,
              onPaymentDetailsClicked = onPaymentDetailsClicked,
              onOpenManualCharge = onOpenManualCharge,
            )
            Spacer(Modifier.height(16.dp))
          }
        }
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
      PullRefreshIndicator(
        refreshing = isRefreshing,
        state = pullRefreshState,
        scale = true,
        modifier = Modifier.align(Alignment.TopCenter),
      )
    }
  }
}

@Composable
private fun PaymentsContent(
  uiState: PaymentsUiState,
  onUpcomingPaymentClicked: (String?) -> Unit,
  onChangeBankAccount: () -> Unit,
  onDiscountClicked: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onPaymentDetailsClicked: () -> Unit,
  onOpenManualCharge: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(Modifier.height(8.dp))
    when (val upcomingPaymentInfo = (uiState as? Content)?.upcomingPaymentInfo) {
      is PaymentFailed -> {
        if (upcomingPaymentInfo.isManualChargeAllowed != null) {
          FailedPaymentInfo(
            amountDue = upcomingPaymentInfo.isManualChargeAllowed.sum.toString(),
            onReviewPaymentClick = onOpenManualCharge,
            modifier = Modifier.padding(horizontal = 16.dp)
          )
          Spacer(Modifier.height(8.dp))
        }
      }

      else -> {}
    }
    val ongoingCharges = (uiState as? Content)?.ongoingCharges
    if (!ongoingCharges.isNullOrEmpty()) {
      OngoingPaymentCards(
        ongoingCharges = ongoingCharges,
        onCardClicked = onUpcomingPaymentClicked,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    val upcomingPayment = (uiState as? Content)?.upcomingPayment
    if (upcomingPayment == NoUpcomingPayment) {
      HedvigInformationSection(
        stringResource(Res.string.PAYMENTS_NO_PAYMENTS_IN_PROGRESS),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    } else {
      PaymentAmountCard(
        upcomingPayment = upcomingPayment as? UpcomingPayment.Content,
        onCardClicked = onUpcomingPaymentClicked,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      )
    }
    UpcomingPaymentInfoCard(
      upcomingPaymentInfo = (uiState as? Content)?.upcomingPaymentInfo,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    )
    val showConnectedPaymentInfo = uiState is Content &&
      uiState.connectedPaymentInfo is ConnectedPaymentInfo.NeedsSetup
    AnimatedVisibility(
      visibleState = remember { MutableTransitionState(showConnectedPaymentInfo) }.apply {
        targetState = showConnectedPaymentInfo
      },
      enter = expandVertically(expandFrom = Alignment.CenterVertically),
    ) {
      CardNotConnectedWarningCard(
        connectedPaymentInfo = (uiState as? Content)?.connectedPaymentInfo as? ConnectedPaymentInfo.NeedsSetup,
        onChangeBankAccount = onChangeBankAccount,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      )
    }

    PaymentsListItems(
      uiState,
      onDiscountClicked = onDiscountClicked,
      onPaymentHistoryClicked = onPaymentHistoryClicked,
      onPaymentDetailsClicked = onPaymentDetailsClicked,
    )
    if (uiState is Content) {
      when (uiState.connectedPaymentInfo) {
        ConnectedPaymentInfo.Pending -> {
          HedvigNotificationCard(
            message = stringResource(Res.string.MY_PAYMENT_UPDATING_MESSAGE),
            priority = Info,
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
          )
          HedvigButton(
            text = androidx.compose.ui.res.stringResource(R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT),
            onClick = onChangeBankAccount,
            enabled = true,
            buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
          )
        }

        is ConnectedPaymentInfo.NeedsSetup,
        ConnectedPaymentInfo.Unknown,
        is ConnectedPaymentInfo.Active,
          -> {
        }
      }
    }
  }
}

@Composable
private fun CardNotConnectedWarningCard(
  connectedPaymentInfo: ConnectedPaymentInfo.NeedsSetup?,
  onChangeBankAccount: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  val dueDateToConnect = connectedPaymentInfo?.dueDateToConnect
  val text = if (dueDateToConnect != null) {
    stringResource(
      Res.string.info_card_missing_payment_missing_payments_body,
      dateTimeFormatter.format(dueDateToConnect),
    )
  } else {
    stringResource(Res.string.info_card_missing_payment_body)
  }
  HedvigNotificationCard(
    message = text,
    style = Button(
      buttonText = stringResource(Res.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_TITLE),
      onButtonClick = onChangeBankAccount,
    ),
    priority = NotificationPriority.Attention,
    modifier = modifier,
  )
}

@Composable
private fun UpcomingPaymentInfoCard(
  upcomingPaymentInfo: UpcomingPaymentInfo?,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    when (upcomingPaymentInfo) {
      NoInfo -> {}

      InProgress -> {
        HedvigNotificationCard(
          message = stringResource(Res.string.PAYMENTS_IN_PROGRESS),
          priority = Info,
        )
      }

      is PaymentFailed -> {
        val monthDateFormatter = rememberHedvigMonthDateTimeFormatter()
        if (upcomingPaymentInfo.isManualChargeAllowed == null) {
          Column {
            HedvigNotificationCard(
              priority = NotificationPriority.Attention,
              message = stringResource(
                Res.string.PAYMENTS_MISSED_PAYMENT,
                monthDateFormatter.format(upcomingPaymentInfo.failedPaymentStartDate),
                monthDateFormatter.format(upcomingPaymentInfo.failedPaymentEndDate),
              ),
              style = NotificationDefaults.InfoCardStyle.Default,
            )
          }
        }
      }

      null -> {}
    }
  }
}

@Composable
private fun PaymentsListItems(
  uiState: PaymentsUiState,
  onDiscountClicked: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onPaymentDetailsClicked: () -> Unit,
) {
  val listItemsSideSpacingModifier = Modifier
    .padding(horizontal = 16.dp)
    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
  Column {
    PaymentsListItem(
      text = stringResource(Res.string.PAYMENTS_DISCOUNTS_SECTION_TITLE),
      icon = {
        Icon(
          imageVector = HedvigIcons.Campaign,
          contentDescription = null,
          tint = HedvigTheme.colorScheme.signalGreenElement,
          modifier = Modifier.size(24.dp),
        )
      },
      modifier = Modifier
        .clickable(onClick = onDiscountClicked)
        .then(listItemsSideSpacingModifier)
        .padding(vertical = 16.dp)
        .fillMaxWidth(),
    )
    HorizontalDivider(modifier = listItemsSideSpacingModifier)
    PaymentsListItem(
      text = stringResource(Res.string.PAYMENTS_PAYMENT_HISTORY_BUTTON_LABEL),
      icon = {
        Icon(
          imageVector = HedvigIcons.Clock,
          contentDescription = null,
          modifier = Modifier.size(24.dp),
        )
      },
      modifier = Modifier
        .clickable(onClick = onPaymentHistoryClicked)
        .then(listItemsSideSpacingModifier)
        .padding(vertical = 16.dp)
        .fillMaxWidth(),
    )
    if (uiState is Content) {
      if (uiState.connectedPaymentInfo is ConnectedPaymentInfo.Active) {
        HorizontalDivider(listItemsSideSpacingModifier)
        PaymentsListItem(
          text = stringResource(Res.string.PAYMENTS_PAYMENT_DETAILS_INFO_TITLE),
          icon = {
            Icon(
              imageVector = HedvigIcons.Card,
              contentDescription = null,
              modifier = Modifier.size(24.dp),
            )
          },
          modifier = Modifier
            .clickable(onClick = onPaymentDetailsClicked)
            .then(listItemsSideSpacingModifier)
            .padding(vertical = 16.dp)
            .fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun OngoingPaymentCards(
  ongoingCharges: List<OngoingCharge>,
  onCardClicked: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier, Arrangement.spacedBy(8.dp)) {
    for (ongoingCharge in ongoingCharges) {
      PaymentCard(
        onClick = { onCardClicked(ongoingCharge.id) },
        title = stringResource(Res.string.PAYMENTS_PROCESSING_PAYMENT),
        endSlotText = ongoingCharge.netAmount.toString(),
        subtitle = rememberHedvigDateTimeFormatter().format(ongoingCharge.date),
        showPlaceholder = false,
      )
    }
  }
}

@Composable
private fun PaymentAmountCard(
  upcomingPayment: UpcomingPayment.Content?,
  onCardClicked: (String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val onClick = if (upcomingPayment != null) {
    { onCardClicked(upcomingPayment.id) }
  } else {
    null
  }
  PaymentCard(
    onClick = onClick,
    title = stringResource(Res.string.PAYMENTS_UPCOMING_PAYMENT),
    endSlotText = if (upcomingPayment != null) {
      upcomingPayment.netAmount.toString()
    } else {
      "100 kr >>"
    },
    subtitle = if (upcomingPayment != null) {
      rememberHedvigDateTimeFormatter().format(upcomingPayment.dueDate)
    } else {
      "22 Jul 2024"
    },
    showPlaceholder = upcomingPayment == null,
    modifier = modifier,
  )
}

@Composable
private fun PaymentCard(
  onClick: (() -> Unit)?,
  title: String,
  endSlotText: String,
  subtitle: String,
  showPlaceholder: Boolean,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .fillMaxWidth(),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(
            title,
            Modifier.hedvigPlaceholder(
              visible = showPlaceholder,
              shape = HedvigTheme.shapes.cornerSmall,
              highlight = PlaceholderHighlight.shimmer(),
            ),
          )
        },
        endSlot = {
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .wrapContentWidth(Alignment.End)
              .hedvigPlaceholder(
                visible = showPlaceholder,
                shape = HedvigTheme.shapes.cornerSmall,
                highlight = PlaceholderHighlight.shimmer(),
              ),
          ) {
            HedvigText(
              text = endSlotText,
              textAlign = TextAlign.End,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
              imageVector = HedvigIcons.ChevronRight,
              contentDescription = null,
              tint = HedvigTheme.colorScheme.fillSecondary,
              modifier = Modifier.size(16.dp),
            )
          }
        },
        spaceBetween = 4.dp,
      )
      Spacer(Modifier.height(2.dp))
      HedvigText(
        text = subtitle,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.hedvigPlaceholder(
          visible = showPlaceholder,
          shape = HedvigTheme.shapes.cornerSmall,
          highlight = PlaceholderHighlight.shimmer(),
        ),
      )
    }
  }
}

@Composable
private fun FailedPaymentInfo(amountDue: String, onReviewPaymentClick: () -> Unit, modifier: Modifier = Modifier) {
  HedvigCard(
    color = HedvigTheme.colorScheme.fillNegative,
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, HedvigTheme.colorScheme.borderPrimary,
        HedvigTheme.shapes.cornerXLarge)
      .hedvigDropShadow()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .background(
              color = HedvigTheme.colorScheme.signalRedFill,
              shape = CircleShape,
            )
            .padding(8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = HedvigIcons.WarningFilled,
            contentDescription = null,
            tint = HedvigTheme.colorScheme.signalRedElement,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(
          modifier = Modifier
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          HedvigText(
            text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_TITLE),
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textPrimary,
          )
          HedvigText(
            text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_AMOUNT_DUE, amountDue),
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_BODY),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )

      Spacer(Modifier.height(12.dp))
      HedvigButton(
        text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_BUTTON),
        onClick = onReviewPaymentClick,
        enabled = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        buttonSize = ButtonDefaults.ButtonSize.Small,
      )
    }
  }
}

@Composable
private fun PaymentsListItem(
  text: String,
  icon: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  endSlot: @Composable () -> Unit = {},
) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        icon()
        HedvigText(text)
      }
    },
    endSlot = { endSlot() },
    modifier = modifier,
    spaceBetween = 8.dp,
  )
}

@Composable
@HedvigPreview
private fun PreviewFailedPaymentInfo() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      FailedPaymentInfo(
        amountDue = "233 kr",
        {},
      )
    }
  }
}


@Composable
@HedvigPreview
private fun PreviewPaymentScreen(
  @PreviewParameter(PaymentsStatePreviewProvider::class) uiState: PaymentsUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PaymentsScreen(
        uiState = uiState,
        { _ -> },
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class PaymentsStatePreviewProvider : CollectionPreviewParameterProvider<PaymentsUiState>(
  buildList {
    add(Error)
    add(Loading)
    add(
      Content(
        isRetrying = false,
        upcomingPayment = NoUpcomingPayment,
        upcomingPaymentInfo = NoInfo,
        ongoingCharges = listOf(OngoingCharge("id", LocalDate.fromEpochDays(401), UiMoney(200.0, SEK))),
        connectedPaymentInfo = ConnectedPaymentInfo.Active(
          "Card",
          "****1234",
        ),
      ),
    )
    add(
      Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, SEK),
          System.now().toLocalDateTime(TimeZone.UTC).date,
          "rdg",
        ),
        upcomingPaymentInfo = NoInfo,
        ongoingCharges = emptyList(),
        connectedPaymentInfo = ConnectedPaymentInfo.Active(
          "Card",
          "****1234",
        ),
      ),
    )
    add(
      Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, SEK),
          System.now().toLocalDateTime(TimeZone.UTC).date,
          "iky",
        ),
        upcomingPaymentInfo = InProgress,
        ongoingCharges = emptyList(),
        connectedPaymentInfo = ConnectedPaymentInfo.Active(
          "Card",
          "****1234",
        ),
      ),
    )
    add(
      Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, SEK),
          System.now().toLocalDateTime(TimeZone.UTC).date,
          "pwe",
        ),
        upcomingPaymentInfo = PaymentFailed(
          System.now().toLocalDateTime(TimeZone.UTC).date,
          System.now().minus(30.days).toLocalDateTime(TimeZone.UTC).date,
          isManualChargeAllowed = ManualChargeToPrompt(
            UiMoney(200.0, UiCurrencyCode.SEK),
          ),
        ),
        ongoingCharges = emptyList(),
        connectedPaymentInfo = ConnectedPaymentInfo.Active(
          "Card",
          "****1234",
        ),
      ),
    )
    add(
      Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, SEK),
          System.now().toLocalDateTime(TimeZone.UTC).date,
          "fkjse",
        ),
        upcomingPaymentInfo = NoInfo,
        ongoingCharges = emptyList(),
        connectedPaymentInfo = ConnectedPaymentInfo.Pending,
      ),
    )
    add(
      Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, SEK),
          System.now().toLocalDateTime(TimeZone.UTC).date,
          "qrdfgeth",
        ),
        upcomingPaymentInfo = PaymentFailed(
          System.now().toLocalDateTime(TimeZone.UTC).date,
          System.now().minus(30.days).toLocalDateTime(TimeZone.UTC).date,
          isManualChargeAllowed = ManualChargeToPrompt(
            UiMoney(200.0, UiCurrencyCode.SEK),
          ),
        ),
        ongoingCharges = emptyList(),
        connectedPaymentInfo = ConnectedPaymentInfo.NeedsSetup(
          null,
        ),
      ),
    )
    add(
      Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, SEK),
          System.now().toLocalDateTime(TimeZone.UTC).date,
          "qrdfgeth2",
        ),
        upcomingPaymentInfo = NoInfo,
        ongoingCharges = emptyList(),
        connectedPaymentInfo = ConnectedPaymentInfo.NeedsSetup(
          null,
        ),
      ),
    )
    add(
      Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, SEK),
          System.now().toLocalDateTime(TimeZone.UTC).date,
          "w345423t6",
        ),
        upcomingPaymentInfo = PaymentFailed(
          System.now().toLocalDateTime(TimeZone.UTC).date,
          System.now().minus(30.days).toLocalDateTime(TimeZone.UTC).date,
          isManualChargeAllowed = ManualChargeToPrompt(
            UiMoney(200.0, UiCurrencyCode.SEK),
          ),
        ),
        ongoingCharges = emptyList(),
        connectedPaymentInfo = ConnectedPaymentInfo.NeedsSetup(
          dueDateToConnect = System.now().plus(30.days).toLocalDateTime(TimeZone.UTC).date,
        ),
      ),
    )
    add(
      Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, SEK),
          System.now().toLocalDateTime(TimeZone.UTC).date,
          "42345",
        ),
        upcomingPaymentInfo = PaymentFailed(
          System.now().toLocalDateTime(TimeZone.UTC).date,
          System.now().minus(30.days).toLocalDateTime(TimeZone.UTC).date,
          isManualChargeAllowed = ManualChargeToPrompt(
            UiMoney(200.0, UiCurrencyCode.SEK),
          ),
        ),
        ongoingCharges = emptyList(),
        connectedPaymentInfo = ConnectedPaymentInfo.NeedsSetup(
          System.now().plus(30.days).toLocalDateTime(TimeZone.UTC).date,
        ),
      ),
    )
  },
)
