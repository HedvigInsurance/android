package com.hedvig.app.feature.offer.ui.composable.insurely

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import java.util.Locale

@Composable
fun LoadingRetrieval(locale: Locale) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 24.dp),
  ) {
    val resources = LocalContext.current.resources
    Text(
      text = resources.getQuantityString(
        hedvig.resources.R.plurals.offer_switcher_title,
        2,
      ).uppercase(locale),
      style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    )
    Spacer(Modifier.height(24.dp))
    CircularProgressIndicator()
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.offer_screen_insurely_card_loading_support_text),
      style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewLoadingRetrieval() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      LoadingRetrieval(Locale.ENGLISH)
    }
  }
}
