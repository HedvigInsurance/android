package com.hedvig.android.feature.changeaddress.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun AddressInfoCard(
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    shape = MaterialTheme.shapes.squircle,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.infoContainer,
      contentColor = MaterialTheme.colorScheme.onInfoContainer,
    ),
    modifier = modifier,
  ) {
    Row(modifier = Modifier.padding(12.dp)) {
      Icon(
        imageVector = Icons.Default.Info,
        contentDescription = "info",
        modifier = Modifier.padding(top = 2.dp).size(16.dp).padding(1.dp),
        tint = MaterialTheme.colorScheme.infoElement,
      )
      Spacer(modifier = Modifier.padding(start = 8.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.CHANGE_ADDRESS_COVERAGE_INFO_TEXT),
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAddressInfoCard() {
  HedvigTheme(flipBackgroundAndSurface = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      Box(modifier = Modifier.padding(8.dp)) {
        AddressInfoCard()
      }
    }
  }
}
