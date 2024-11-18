package com.hedvig.android.feature.payments.ui.payments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle
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
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.Campaign
import com.hedvig.android.design.system.hedvig.icon.Card
import com.hedvig.android.design.system.hedvig.icon.ChevronRight
import com.hedvig.android.design.system.hedvig.icon.Clock
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.placeholder.PlaceholderHighlight
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.design.system.hedvig.placeholder.shimmer
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.ConnectedPaymentInfo.Connected
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.ConnectedPaymentInfo.NotConnected
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.ConnectedPaymentInfo.Pending
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPayment
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPaymentInfo
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import hedvig.resources.R
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun PaymentsDestination(
  viewModel: PaymentsViewModel,
  onUpcomingPaymentClicked: (memberChargeId: String?) -> Unit,
  onDiscountClicked: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onChangeBankAccount: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PaymentsScreen(
    uiState = uiState,
    onUpcomingPaymentClicked = onUpcomingPaymentClicked,
    onChangeBankAccount = onChangeBankAccount,
    onDiscountClicked = onDiscountClicked,
    onPaymentHistoryClicked = onPaymentHistoryClicked,
    onRetry = { viewModel.emit(PaymentsEvent.Retry) },
  )
}

@Composable
private fun PaymentsScreen(
  uiState: PaymentsUiState,
  onUpcomingPaymentClicked: (memberChargeId: String?) -> Unit,
  onChangeBankAccount: () -> Unit,
  onDiscountClicked: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onRetry: () -> Unit,
) {
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val isRefreshing =
    uiState is PaymentsUiState.Loading || uiState.safeCast<PaymentsUiState.Content>()?.isRetrying == true
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
            .padding(horizontal = 16.dp),
      ) {
        HedvigText(
          text = stringResource(R.string.TAB_REFERRALS_TITLE),
          style = HedvigTheme.typography.headlineSmall,
        )
      }
      Spacer(Modifier.height(8.dp)) //todo: maybe 16?
      when (uiState) {
        PaymentsUiState.Error -> HedvigErrorSection(onButtonClick = onRetry, Modifier.weight(1f))
        else -> {
          PaymentsContent(
            uiState = uiState,
            onUpcomingPaymentClicked = { upcomingPayment ->
              onUpcomingPaymentClicked(upcomingPayment.id)
            },
            onChangeBankAccount = onChangeBankAccount,
            onDiscountClicked = onDiscountClicked,
            onPaymentHistoryClicked = onPaymentHistoryClicked,
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

@Composable
private fun PaymentsContent(
  uiState: PaymentsUiState,
  onUpcomingPaymentClicked: (UpcomingPayment.Content) -> Unit,
  onChangeBankAccount: () -> Unit,
  onDiscountClicked: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    val upcomingPayment = (uiState as? PaymentsUiState.Content)?.upcomingPayment
    if (upcomingPayment == UpcomingPayment.NoUpcomingPayment) {
      HedvigInformationSection(stringResource(R.string.PAYMENTS_NO_PAYMENTS_IN_PROGRESS))
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
      upcomingPaymentInfo = (uiState as? PaymentsUiState.Content)?.upcomingPaymentInfo,
      modifier = Modifier
          .padding(horizontal = 16.dp)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    )
    val showConnectedPaymentInfo = uiState is PaymentsUiState.Content &&
      uiState.connectedPaymentInfo is NotConnected &&
      uiState.connectedPaymentInfo.allowChangingConnectedBankAccount
    AnimatedVisibility(
      visibleState = remember { MutableTransitionState(showConnectedPaymentInfo) }.apply {
        targetState = showConnectedPaymentInfo
      },
      enter = expandVertically(expandFrom = Alignment.CenterVertically),
    ) {
      CardNotConnectedWarningCard(
        connectedPaymentInfo = (uiState as? PaymentsUiState.Content)?.connectedPaymentInfo as? NotConnected,
        onChangeBankAccount = onChangeBankAccount,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      )
    }

    PaymentsListItems(uiState, onDiscountClicked, onPaymentHistoryClicked)
    if (uiState is PaymentsUiState.Content) {
      when (val connectedPaymentInfo = uiState.connectedPaymentInfo) {
        is Connected -> {
          if (connectedPaymentInfo.allowChangingConnectedBankAccount) {
            Spacer(Modifier.weight(1f))
            HedvigButton(
              text = stringResource(R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT),
              onClick = onChangeBankAccount,
              enabled = true,
              buttonStyle = ButtonStyle.Secondary,
              modifier = Modifier
                  .padding(horizontal = 16.dp)
                  .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                  .hedvigPlaceholder(uiState.isRetrying, highlight = PlaceholderHighlight.shimmer()),
            )
          }
        }

        is NotConnected -> {}

        Pending -> {
          HedvigNotificationCard(
            message = stringResource(R.string.MY_PAYMENT_UPDATING_MESSAGE),
            priority = NotificationDefaults.NotificationPriority.Info,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
          )
        }
      }
    }
  }
}

@Composable
private fun CardNotConnectedWarningCard(
  connectedPaymentInfo: NotConnected?,
  onChangeBankAccount: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  val text = if (connectedPaymentInfo?.dueDateToConnect != null) {
    stringResource(
      R.string.info_card_missing_payment_body_with_date,
      dateTimeFormatter.format(connectedPaymentInfo.dueDateToConnect.toJavaLocalDate()),
    )
  } else {
    stringResource(id = R.string.info_card_missing_payment_body)
  }
  HedvigNotificationCard(
    message = text,
    style = NotificationDefaults.InfoCardStyle.Button(
      buttonText = stringResource(id = R.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_TITLE),
      onButtonClick = onChangeBankAccount,
    ),
    priority = NotificationDefaults.NotificationPriority.Attention,
    modifier = modifier,
  )
}

@Composable
private fun UpcomingPaymentInfoCard(upcomingPaymentInfo: UpcomingPaymentInfo?, modifier: Modifier = Modifier) {
  Box(modifier) {
    when (upcomingPaymentInfo) {
      UpcomingPaymentInfo.NoInfo -> {}
      UpcomingPaymentInfo.InProgress -> {
        HedvigNotificationCard(
          message = stringResource(id = R.string.PAYMENTS_IN_PROGRESS),
          priority = NotificationDefaults.NotificationPriority.Info,
        )
      }

      is UpcomingPaymentInfo.PaymentFailed -> {
        val monthDateFormatter = rememberHedvigMonthDateTimeFormatter()
        HedvigNotificationCard(
          priority = NotificationDefaults.NotificationPriority.Attention,
          message = stringResource(
            R.string.PAYMENTS_MISSED_PAYMENT,
            monthDateFormatter.format(upcomingPaymentInfo.failedPaymentStartDate.toJavaLocalDate()),
            monthDateFormatter.format(upcomingPaymentInfo.failedPaymentEndDate.toJavaLocalDate()),
          ),
        )
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
) {
  val listItemsSideSpacingModifier =
      Modifier
          .padding(horizontal = 16.dp)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
  Column {
    PaymentsListItem(
      text = stringResource(R.string.PAYMENTS_DISCOUNTS_SECTION_TITLE),
      icon = {
        Icon(
          imageVector = HedvigIcons.Campaign,
          contentDescription = null,
          tint = HedvigTheme.colorScheme.signalGreenElement, //todo: check here!
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
      text = stringResource(R.string.PAYMENTS_PAYMENT_HISTORY_BUTTON_LABEL),
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
    if (uiState is PaymentsUiState.Content) {
      if (uiState.connectedPaymentInfo is Connected) {
        HorizontalDivider(listItemsSideSpacingModifier)
        PaymentsListItem(
          text = uiState.connectedPaymentInfo.displayName,
          icon = {
            Icon(
              imageVector = HedvigIcons.Card,
              contentDescription = null,
              modifier = Modifier.size(24.dp),
            )
          },
          endSlot = {
            HedvigText(
              text = uiState.connectedPaymentInfo.maskedAccountNumber,
              color = HedvigTheme.colorScheme.textSecondary,
              textAlign = TextAlign.End,
            )
          },
          modifier = listItemsSideSpacingModifier
              .padding(vertical = 16.dp)
              .fillMaxWidth()
              .hedvigPlaceholder(uiState.isRetrying, highlight = PlaceholderHighlight.shimmer()),
        )
      }
    }
  }
}

@Composable
private fun PaymentAmountCard(
  upcomingPayment: UpcomingPayment.Content?,
  onCardClicked: (UpcomingPayment.Content) -> Unit,
  modifier: Modifier = Modifier,
) {
  val onClick = if (upcomingPayment != null) {
    { onCardClicked(upcomingPayment) }
  } else {
    null
  }
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
            stringResource(R.string.PAYMENTS_UPCOMING_PAYMENT),
            Modifier.hedvigPlaceholder(
              visible = upcomingPayment == null,
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
                    visible = upcomingPayment == null,
                    highlight = PlaceholderHighlight.shimmer(),
                ),
          ) {
            HedvigText(
              text = if (upcomingPayment != null) {
                upcomingPayment.netAmount.toString()
              } else {
                "100 kr >>"
              },
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
        text = if (upcomingPayment != null) {
          rememberHedvigDateTimeFormatter().format(upcomingPayment.dueDate.toJavaLocalDate())
        } else {
          "22 Jul 2024"
        },
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.hedvigPlaceholder(
          visible = upcomingPayment == null,
          highlight = PlaceholderHighlight.shimmer(),
        ),
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
  )
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
      )
    }
  }
}

private class PaymentsStatePreviewProvider : CollectionPreviewParameterProvider<PaymentsUiState>(
  buildList {
    add(PaymentsUiState.Error)
    add(PaymentsUiState.Loading)
    add(
      PaymentsUiState.Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.NoUpcomingPayment,
        upcomingPaymentInfo = UpcomingPaymentInfo.NoInfo,
        connectedPaymentInfo = Connected(
          "Card",
          "****1234",
          true,
        ),
      ),
    )
    add(
      PaymentsUiState.Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, UiCurrencyCode.SEK),
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          "rdg",
        ),
        upcomingPaymentInfo = UpcomingPaymentInfo.NoInfo,
        connectedPaymentInfo = Connected(
          "Card",
          "****1234",
          true,
        ),
      ),
    )
    add(
      PaymentsUiState.Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, UiCurrencyCode.SEK),
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          "iky",
        ),
        upcomingPaymentInfo = UpcomingPaymentInfo.InProgress,
        connectedPaymentInfo = Connected(
          "Card",
          "****1234",
          true,
        ),
      ),
    )
    add(
      PaymentsUiState.Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, UiCurrencyCode.SEK),
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          "pwe",
        ),
        upcomingPaymentInfo = UpcomingPaymentInfo.PaymentFailed(
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          Clock.System.now().minus(30.days).toLocalDateTime(TimeZone.UTC).date,
        ),
        connectedPaymentInfo = Connected(
          "Card",
          "****1234",
          true,
        ),
      ),
    )
    add(
      PaymentsUiState.Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, UiCurrencyCode.SEK),
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          "fkjse",
        ),
        upcomingPaymentInfo = UpcomingPaymentInfo.NoInfo,
        connectedPaymentInfo = Pending,
      ),
    )
    add(
      PaymentsUiState.Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, UiCurrencyCode.SEK),
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          "qrdfgeth",
        ),
        upcomingPaymentInfo = UpcomingPaymentInfo.NoInfo,
        connectedPaymentInfo = NotConnected(
          null,
          true,
        ),
      ),
    )
    add(
      PaymentsUiState.Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, UiCurrencyCode.SEK),
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          "qrdfgeth2",
        ),
        upcomingPaymentInfo = UpcomingPaymentInfo.NoInfo,
        connectedPaymentInfo = NotConnected(
          null,
          false,
        ),
      ),
    )
    add(
      PaymentsUiState.Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, UiCurrencyCode.SEK),
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          "w345423t6",
        ),
        upcomingPaymentInfo = UpcomingPaymentInfo.PaymentFailed(
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          Clock.System.now().minus(30.days).toLocalDateTime(TimeZone.UTC).date,
        ),
        connectedPaymentInfo = NotConnected(
          null,
          true,
        ),
      ),
    )
    add(
      PaymentsUiState.Content(
        isRetrying = false,
        upcomingPayment = UpcomingPayment.Content(
          UiMoney(100.0, UiCurrencyCode.SEK),
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          "42345",
        ),
        upcomingPaymentInfo = UpcomingPaymentInfo.PaymentFailed(
          Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
          Clock.System.now().minus(30.days).toLocalDateTime(TimeZone.UTC).date,
        ),
        connectedPaymentInfo = NotConnected(
          Clock.System.now().plus(30.days).toLocalDateTime(TimeZone.UTC).date,
          false,
        ),
      ),
    )
  },
)
