package com.hedvig.app.feature.offer.ui.composable.variants

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun VariantHeader() {
  Text(
    text = stringResource(id = hedvig.resources.R.string.offer_screen_varianted_offers_header),
    style = MaterialTheme.typography.headlineSmall,
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .padding(bottom = 4.dp, top = 40.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewVariantHeader() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      VariantHeader()
    }
  }
}
