package com.hedvig.android.core.ui.text

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled

@Composable
fun WarningTextWithIcon(text: String, modifier: Modifier = Modifier) {
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

@Composable
fun WarningTextWithIconForInput(text: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = Icons.Hedvig.WarningFilled,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.warningElement,
      modifier = Modifier.size(14.dp),
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = text,
      color = MaterialTheme.colorScheme.warningElement,
      style = MaterialTheme.typography.labelMedium,
    )
  }
}
