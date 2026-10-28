package com.hedvig.android.feature.payoutaccount.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.PaymentMethodPillow
import com.hedvig.android.design.system.hedvig.PaymentMethodPillowMarkSize
import com.hedvig.android.design.system.hedvig.PaymentMethodPlusMark
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Bank
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.data.provider
import hedvig.resources.PAYMENTS_ACCOUNT
import hedvig.resources.PAYMENT_METHOD_PENDING_STATUS_LABEL
import hedvig.resources.PAYMENT_PRIMARY_LABEL
import hedvig.resources.Res
import hedvig.resources.swish
import hedvig.resources.trustly
import org.jetbrains.compose.resources.stringResource

/**
 * The provider's brand mark on the tile it is drawn on: white behind the full-colour marks, black
 * behind the monochrome ones, which pick up the tile's content colour.
 */
@Composable
internal fun PayoutProviderPillow(provider: PaymentProvider?, modifier: Modifier = Modifier) {
  val onDarkTile = provider.onDarkTile
  PaymentMethodPillow(
    modifier = modifier,
    containerColor = if (onDarkTile) HedvigTheme.colorScheme.fillBlack else HedvigTheme.colorScheme.fillWhite,
    contentColor = if (onDarkTile) HedvigTheme.colorScheme.fillWhite else HedvigTheme.colorScheme.fillBlack,
    mark = { PayoutProviderMark(provider, Modifier.size(PaymentMethodPillowMarkSize)) },
  )
}

/**
 * The provider's brand mark. The monochrome ones pick up the surrounding content colour; the
 * full-colour ones ignore it. A provider with no mark of its own falls back to the plus, so one the
 * backend adds later still renders.
 */
@Composable
internal fun PayoutProviderMark(provider: PaymentProvider?, modifier: Modifier = Modifier) {
  when (provider) {
    PaymentProvider.Trustly -> Icon(HedvigIcons.Trustly, EmptyContentDescription, modifier)
    PaymentProvider.Nordea -> Image(HedvigIcons.Bank, EmptyContentDescription, modifier)
    PaymentProvider.Swish -> Image(HedvigIcons.Swish, EmptyContentDescription, modifier)
    else -> PaymentMethodPlusMark(modifier)
  }
}

/** Trustly's mark is monochrome, so it reads better inverted on a dark tile than on the white one. */
private val PaymentProvider?.onDarkTile: Boolean
  get() = this == PaymentProvider.Trustly

@Composable
internal fun payoutMethodTitle(method: PayoutAccount): String = when (method) {
  is PayoutAccount.SwishPayout -> stringResource(Res.string.swish)
  is PayoutAccount.Trustly -> formatBankAccountLabel(stringResource(Res.string.trustly), method.bankName)
  is PayoutAccount.BankAccount -> formatBankAccountLabel(stringResource(Res.string.PAYMENTS_ACCOUNT), method.bankName)
}

@Composable
internal fun payoutMethodSubtitle(method: PayoutAccount): String? {
  val value = when (method) {
    is PayoutAccount.SwishPayout -> method.phoneNumber.orEmpty()
    is PayoutAccount.Trustly -> formatBankAccountNumber(method.clearingNumber, method.accountNumber)
    is PayoutAccount.BankAccount -> formatBankAccountNumber(method.clearingNumber, method.accountNumber)
  }
  return when {
    value.isNotBlank() -> value
    method.isPending -> stringResource(Res.string.PAYMENT_METHOD_PENDING_STATUS_LABEL)
    else -> null
  }
}

private fun formatBankAccountLabel(baseLabel: String, bankName: String?): String {
  return if (bankName != null) "$baseLabel - $bankName" else baseLabel
}

private fun formatBankAccountNumber(clearingNumber: String?, accountNumber: String?): String {
  return when {
    clearingNumber != null && accountNumber != null -> "$clearingNumber-$accountNumber"
    else -> clearingNumber.orEmpty()
  }
}

/**
 * The trailing edge of a method row. Sized so a row without one keeps the same height as its
 * neighbours.
 */
@Composable
internal fun PayoutMethodRow(
  method: PayoutAccount,
  modifier: Modifier = Modifier,
  endSlot: @Composable (() -> Unit)? = null,
) {
  Row(
    modifier = modifier
      .heightIn(min = 64.dp)
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    PayoutProviderPillow(method.provider)
    Spacer(Modifier.width(2.dp))
    Column(
      Modifier
        .weight(1f)
        .padding(vertical = 8.dp),
    ) {
      HedvigText(text = payoutMethodTitle(method))
      val subtitle = payoutMethodSubtitle(method)
      if (subtitle != null) {
        HedvigText(
          text = subtitle,
          style = HedvigTheme.typography.finePrint,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
    endSlot?.invoke()
  }
}

/** The same mark, title and subtitle as [PayoutMethodRow], for the screens that list methods as radio options. */
@Composable
internal fun PayoutAccount.toRadioOption(): RadioOption = RadioOption(
  id = RadioOptionId(provider.rawValue),
  text = payoutMethodTitle(this),
  label = payoutMethodSubtitle(this),
)

/** Static counterpart to the button the design draws it with: the primary method is not a choice made here. */
@Composable
internal fun PrimaryMethodLabel(modifier: Modifier = Modifier) {
  Surface(
    shape = HedvigTheme.shapes.cornerXXLarge,
    color = HedvigTheme.colorScheme.buttonSecondaryAltResting,
    modifier = modifier,
  ) {
    HedvigText(
      text = stringResource(Res.string.PAYMENT_PRIMARY_LABEL),
      style = HedvigTheme.typography.label,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
    )
  }
}

@Composable
@HedvigPreview
private fun PreviewPayoutMethodRow(
  @PreviewParameter(PayoutAccountPreviewProvider::class) account: PayoutAccount,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HedvigCard(
        shape = HedvigTheme.shapes.cornerLarge,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      ) {
        PayoutMethodRow(
          account,
          Modifier.fillMaxWidth(),
          endSlot = if (account.isDefault) {
            { PrimaryMethodLabel() }
          } else {
            null
          },
        )
      }
    }
  }
}

private class PayoutAccountPreviewProvider : CollectionPreviewParameterProvider<PayoutAccount>(
  listOf(
    PayoutAccount.SwishPayout(phoneNumber = "070-123 45 67", isPending = false, isDefault = true),
    PayoutAccount.Trustly(
      clearingNumber = "8327",
      accountNumber = "12345678",
      bankName = "Swedbank",
      isPending = false,
      isDefault = false,
    ),
    PayoutAccount.BankAccount(
      clearingNumber = null,
      accountNumber = null,
      bankName = null,
      isPending = true,
      isDefault = false,
    ),
  ),
)
