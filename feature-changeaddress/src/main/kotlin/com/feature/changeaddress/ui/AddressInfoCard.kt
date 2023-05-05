package com.feature.changeaddress.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.R
import com.hedvig.android.core.designsystem.component.card.HedvigCard

@Composable
fun AddressInfoCard() {
  HedvigCard(
    colors = CardDefaults.cardColors(containerColor = Color(0xFFCFE5F2)),
  ) {
    Row(modifier = Modifier.padding(12.dp)) {
      Icon(
        painter = painterResource(R.drawable.ic_info),
        contentDescription = "info",
        modifier = Modifier.offset(y = 2.dp),
      )
      Spacer(modifier = Modifier.padding(start = 8.dp))
      Text(
        text = "Hedvig täcker ditt gamla hem i 30 dagar efter att du flyttat.",
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}
