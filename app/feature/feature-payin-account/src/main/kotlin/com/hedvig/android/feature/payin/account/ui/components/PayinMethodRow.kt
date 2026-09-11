package com.hedvig.android.feature.payin.account.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.PaymentMethodPillow
import com.hedvig.android.design.system.hedvig.PaymentMethodPillowMarkSize
import com.hedvig.android.design.system.hedvig.PaymentMethodPlusMark
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.design.system.hedvig.icon.colored.Kivra
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import com.hedvig.android.feature.payin.account.data.InvoiceDelivery
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.provider
import com.hedvig.android.feature.payin.account.data.toDeliveryString
import hedvig.resources.PAYMENTS_BANK_LABEL
import hedvig.resources.PAYMENT_PRIMARY_LABEL
import hedvig.resources.REFERRAL_PENDING_STATUS_LABEL
import hedvig.resources.Res
import hedvig.resources.swish
import octopus.type.MemberPaymentProvider
import org.jetbrains.compose.resources.stringResource

/**
 * The method's brand mark on the tile it is drawn on: white behind the full-colour marks, black
 * behind Trustly's monochrome one, which picks up the tile's content colour.
 */
@Composable
internal fun PayinProviderPillow(provider: MemberPaymentProvider?, modifier: Modifier = Modifier) {
  val onDarkTile = provider == MemberPaymentProvider.TRUSTLY
  PaymentMethodPillow(
    modifier = modifier,
    containerColor = if (onDarkTile) HedvigTheme.colorScheme.fillBlack else HedvigTheme.colorScheme.fillWhite,
    contentColor = if (onDarkTile) HedvigTheme.colorScheme.fillWhite else HedvigTheme.colorScheme.fillBlack,
    mark = { PayinProviderMark(provider, Modifier.size(PaymentMethodPillowMarkSize)) },
  )
}

/**
 * The method's brand mark. Trustly's is monochrome and picks up the surrounding content colour; the
 * others are full-colour and ignore it. A provider with no mark of its own falls back to the plus,
 * so one the backend adds later still renders.
 */
@Composable
internal fun PayinProviderMark(provider: MemberPaymentProvider?, modifier: Modifier = Modifier) {
  when (provider) {
    MemberPaymentProvider.TRUSTLY -> Icon(HedvigIcons.Trustly, EmptyContentDescription, modifier)
    MemberPaymentProvider.SWISH -> Image(HedvigIcons.Swish, EmptyContentDescription, modifier)
    MemberPaymentProvider.INVOICE -> Image(HedvigIcons.Kivra, EmptyContentDescription, modifier)
    else -> PaymentMethodPlusMark(modifier)
  }
}

@Composable
internal fun PayinMethodMark(method: PayinAccount, modifier: Modifier = Modifier) {
  PayinProviderMark(method.provider, modifier)
}

@Composable
internal fun payinMethodTitle(method: PayinAccount): String = when (method) {
  is PayinAccount.Trustly -> stringResource(Res.string.PAYMENTS_BANK_LABEL)
  is PayinAccount.SwishPayin -> stringResource(Res.string.swish)
  is PayinAccount.Invoice -> method.delivery.toDeliveryString().orEmpty()
}

@Composable
internal fun payinMethodSubtitle(method: PayinAccount): String? {
  val pendingLabel = stringResource(Res.string.REFERRAL_PENDING_STATUS_LABEL)
  return when (method) {
    is PayinAccount.Trustly -> {
      method.maskedAccount() ?: pendingLabel.takeIf { method.isPending }
    }

    is PayinAccount.SwishPayin -> {
      val phoneNumber = method.phoneNumber
      if (phoneNumber.isNullOrBlank()) pendingLabel.takeIf { method.isPending } else formatSwishPhoneNumber(phoneNumber)
    }

    is PayinAccount.Invoice -> {
      null
    }
  }
}

/**
 * The trailing edge of a method row. Sized so a row without one keeps the same height as its
 * neighbours.
 */
@Composable
internal fun PayinMethodRow(
  method: PayinAccount,
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
    PayinProviderPillow(method.provider)
    Spacer(Modifier.width(2.dp))
    Column(
      Modifier
        .weight(1f)
        .padding(vertical = 8.dp),
    ) {
      HedvigText(text = payinMethodTitle(method))
      val subtitle = payinMethodSubtitle(method)
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

/** The same mark, title and subtitle as [PayinMethodRow], for the screens that list methods as radio options. */
@Composable
internal fun PayinAccount.toRadioOption(): RadioOption = RadioOption(
  id = RadioOptionId(provider.rawValue),
  text = payinMethodTitle(this),
  label = payinMethodSubtitle(this),
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

private fun PayinAccount.Trustly.maskedAccount(): String? {
  val bank = bankName ?: return null
  val lastFour = accountNumber?.takeLast(4)?.takeIf { it.isNotBlank() } ?: return bank
  return "$bank ···· $lastFour"
}

internal fun PayinAccount.Trustly.maskedAccountNumber(): String? {
  val whole = "${clearingNumber?.takeIf{ it.isNotBlank() } ?: ""}${accountNumber?.takeIf{ it.isNotBlank() } ?: ""}"
  if (whole.length > 8) {
    val lastFour = whole.takeLast(4).takeIf { it.isNotBlank() } ?: return null
    return "**** $lastFour"
  } else return null

}

internal fun formatSwishPhoneNumber(phoneNumber: String): String {
  val digits = phoneNumber.take(15)
  val sb = StringBuilder()
  for (i in digits.indices) {
    sb.append(digits[i])
    if (i in setOf(2, 5, 7)) {
      sb.append("-")
    }
  }
  return sb.toString()
}

@Composable
@HedvigPreview
private fun PreviewPayinMethodRow(
  @PreviewParameter(PayinAccountPreviewProvider::class) account: PayinAccount,
) {
  HedvigTheme {
    Surface(color = colorScheme.backgroundPrimary) {
      HedvigCard(
        shape = HedvigTheme.shapes.cornerLarge,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      ) {
        PayinMethodRow(
          account,
          Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

private class PayinAccountPreviewProvider : CollectionPreviewParameterProvider<PayinAccount>(
  listOf(
    PayinAccount.Trustly(
      clearingNumber = "****",
      accountNumber = "*45678",
      bankName = "Swedbank",
      isPending = false,
      isDefault = true,
    ),
    PayinAccount.SwishPayin(
      phoneNumber = "0701234567",
      isPending = false,
      isDefault = false,
    ),
    PayinAccount.Invoice(
      delivery = InvoiceDelivery.Kivra,
      isPending = false,
      isDefault = false,
      email = "",
    ),
  ),
)
