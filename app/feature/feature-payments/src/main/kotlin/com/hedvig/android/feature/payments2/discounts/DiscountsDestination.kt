package com.hedvig.android.feature.payments2.discounts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.payments2.DiscountRow
import com.hedvig.android.feature.payments2.data.Discount
import com.hedvig.android.feature.payments2.discountsPreviewData
import hedvig.resources.R

@Composable
internal fun DiscountsDestination(
  discounts: List<Discount>,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(id = R.string.PAYMENTS_DISCOUNTS_SECTION_TITLE),
    navigateUp = navigateUp,
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      if (discounts.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(id = R.string.PAYMENTS_CAMPAIGNS_INFO_TITLE))
        discounts.forEachIndexed { index, discount ->
          DiscountRow(discount)
          if (index < discounts.size - 1) {
            Divider()
          }
        }
      }
      HedvigSecondaryContainedButton(
        text = stringResource(id = R.string.PAYMENTS_ADD_CAMPAIGN_CODE),
        onClick = { }, // TODO
      )
    }
  }
}

@Composable
@HedvigPreview
private fun PaymentDetailsScreenPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DiscountsDestination(
        discounts = discountsPreviewData,
        {},
      )
    }
  }
}


