package com.hedvig.app.feature.insurance.ui.terminatedcontracts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronRight
import hedvig.resources.R

@Composable
fun TerminatedContractsButton(nrOfTerminatedContracts: Int, onClick: () -> Unit) {
  HedvigCard(
    onClick = onClick,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    modifier = Modifier.padding(16.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
    ) {
      Column(Modifier.weight(1f, true)) {
        val text = LocalContext.current.resources.getQuantityString(
          R.plurals.insurances_tab_terminated_insurance_subtitile,
          nrOfTerminatedContracts,
          nrOfTerminatedContracts,
        )
        Text(text = text)
      }
      Icon(
        imageVector = Icons.Hedvig.ChevronRight,
        modifier = Modifier.size(16.dp),
        contentDescription = null,
      )
    }
  }
}

@Composable
@HedvigPreview
fun TerminatedContractsButtonPreview() {
  HedvigTheme(useNewColorScheme = true) {
    Surface {
      TerminatedContractsButton(nrOfTerminatedContracts = 3) {}
    }
  }
}
