package com.hedvig.android.feature.payments2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.information.HedvigPill
import com.hedvig.android.core.designsystem.material3.onSecondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.secondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.payments2.data.Discount
import hedvig.resources.R
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
fun DiscountRow(discount: Discount) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  Column(modifier = Modifier.padding(vertical = 16.dp)) {
    Row {
      HedvigPill(
        text = discount.code,
        color = MaterialTheme.colorScheme.secondaryContainedButtonContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainedButtonContainer,
      )
      discount.amount?.let {
        Text(
          text = discount.amount.toString(),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.End,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }

    Spacer(Modifier.height(8.dp))

    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        discount.displayName?.let {
          Text(
            text = it,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
          )
        }
      },
      endSlot = {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        if (discount.expiresAt != null && discount.isExpired(today)) {
          Text(
            text = stringResource(
              id = R.string.PAYMENTS_EXPIRED_DATE,
              dateTimeFormatter.format(discount.expiresAt.toJavaLocalDate()),
            ),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth(),
          )
        } else if (discount.expiresAt != null) {
          Text(
            text = stringResource(
              id = R.string.PAYMENTS_VALID_UNTIL,
              dateTimeFormatter.format(discount.expiresAt.toJavaLocalDate()),
            ),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
    )

    val text = if (discount.isReferral) {
      stringResource(id = R.string.PAYMENTS_REFERRAL_DISCOUNT)
    } else {
      discount.description
    }

    text?.let {
      Text(
        text = it,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
      )
    }
  }
}

@Composable
@HedvigPreview
fun DiscountRowPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        discountsPreviewData.forEach {
          DiscountRow(discount = it)
        }
      }
    }
  }
}
