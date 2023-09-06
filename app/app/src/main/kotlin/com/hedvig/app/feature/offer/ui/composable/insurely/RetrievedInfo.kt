package com.hedvig.app.feature.offer.ui.composable.insurely

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hedvig.android.apollo.format
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.offer.ui.OfferItems
import com.hedvig.app.util.compose.preview.previewData
import java.util.Locale
import javax.money.MonetaryAmount

@Composable
fun RetrievedInfo(
  data: OfferItems.InsurelyCard.Retrieved,
  locale: Locale,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
  ) {
    if (data.savedWithHedvig != null) {
      SavedWithHedvigChip(data.savedWithHedvig)
      Spacer(Modifier.height(6.dp))
    }
    Spacer(Modifier.height(8.dp))
    val resources = LocalContext.current.resources
    Text(
      text = when {
        data.currentInsurances.size > 1 -> {
          resources.getQuantityString(hedvig.resources.R.plurals.offer_switcher_title, data.currentInsurances.size)
        }
        data.insuranceProviderDisplayName != null -> {
          stringResource(
            hedvig.resources.R.string.offer_screen_insurely_card_your_insurance_with,
            data.insuranceProviderDisplayName,
          )
        }
        else -> {
          resources.getQuantityString(hedvig.resources.R.plurals.offer_switcher_title, 1)
        }
      }.uppercase(locale),
      style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    )
    Spacer(Modifier.height(24.dp))
    Text(
      text = data.totalNetPremium?.format(locale) ?: "",
      style = MaterialTheme.typography.headlineMedium,
    )
    Text(
      text = stringResource(hedvig.resources.R.string.OFFER_PRICE_PER_MONTH),
      style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    )
    if (data.currentInsurances.size <= 1) {
      Spacer(Modifier.height(8.dp))
    } else {
      Spacer(Modifier.height(16.dp))
      Divider()
      Spacer(Modifier.height(16.dp))
      CurrentInsurancesList(data, locale)
    }
  }
}

@Composable
private fun SavedWithHedvigChip(savedWithHedvig: MonetaryAmount) {
  HedvigCard(
    shape = RoundedCornerShape(4.dp),
    colors = CardDefaults.outlinedCardColors(
      MaterialTheme.colorScheme.secondary,
    ),
  ) {
    Text(
      text = stringResource(
        hedvig.resources.R.string.offer_screen_insurely_card_cost_difference_info,
        savedWithHedvig.number,
      ),
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
    )
  }
}

@Composable
private fun CurrentInsurancesList(
  data: OfferItems.InsurelyCard.Retrieved,
  locale: Locale,
) {
  data.currentInsurances.forEach { insurance ->
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .height(48.dp)
        .fillMaxWidth(),
    ) {
      Text(
        text = insurance.name,
        style = MaterialTheme.typography.bodyLarge,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = insurance.amount.format(locale),
        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewRetrievedInfo() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      RetrievedInfo(
        OfferItems.InsurelyCard.Retrieved.previewData(),
        Locale.ENGLISH,
      )
    }
  }
}
