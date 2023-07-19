package com.hedvig.android.feature.claimtriaging

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.warningElement

@Composable
fun WarningTextWithIcon(modifier: Modifier = Modifier, text: String) {
  Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
    Icon(
      imageVector = Icons.Default.Info,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.warningElement,
    )
    Spacer(Modifier.width(8.dp))
    Text(text)
  }
}
