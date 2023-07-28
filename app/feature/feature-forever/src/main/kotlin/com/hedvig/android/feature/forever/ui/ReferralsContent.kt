package com.hedvig.android.feature.forever.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.apollo.format
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Copy
import com.hedvig.android.feature.forever.ForeverUiState
import com.hedvig.android.feature.forever.copyToClipboard
import hedvig.resources.R
import java.util.*
import org.javamoney.moneta.Money


@Suppress("UnusedReceiverParameter")
@Composable
internal fun ColumnScope.ReferralsContent(
  uiState: ForeverUiState,
) {
  val locale = uiState.locale ?: Locale.ENGLISH
  val context = LocalContext.current

  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
      text = uiState.grossPriceAmount?.format(locale) ?: "-",
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
  }
  Spacer(Modifier.height(16.dp))
  /*
  AndroidViewBinding(
    factory = ReferralsHeaderBinding::inflate,
    update = bindPieChart(uiState),
  )
   */
  Spacer(Modifier.height(24.dp))
  if (uiState.referrals.isEmpty() && uiState.incentive != null) {
    Spacer(Modifier.height(32.dp))
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
      Text(
        text = stringResource(
          id = R.string.referrals_empty_body,
          uiState.incentive.format(locale),
          Money.of(0, uiState.incentive.currency?.currencyCode).format(locale),
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
  } else {
    Text(
      text = stringResource(id = R.string.FOREVER_TAB_MONTLY_COST_LABEL),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
      Text(
        text = stringResource(
          id = R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
          uiState.currentNetAmount?.format(locale) ?: "-",
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(82.dp))
  }
  HedvigBigCard(
    onClick = {
      uiState.campaignCode?.let {
        context.copyToClipboard(uiState.campaignCode)
      }
    },
    enabled = true,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Row(
      modifier = Modifier
        .heightIn(min = 72.dp)
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Column {
        Text(
          text = stringResource(id = R.string.referrals_empty_code_headline),
          style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
        Text(
          text = uiState.campaignCode ?: "",
          style = MaterialTheme.typography.headlineSmall,
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      Icon(
        imageVector = Icons.Hedvig.Copy,
        contentDescription = "Copy",
        modifier = Modifier
          .align(Alignment.Bottom)
          .padding(bottom = 8.dp),
      )
    }
  }
}
