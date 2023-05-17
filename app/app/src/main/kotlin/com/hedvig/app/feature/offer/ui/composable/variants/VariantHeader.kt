package com.hedvig.app.feature.offer.ui.composable.variants

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
    style = MaterialTheme.typography.h6,
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .padding(bottom = 4.dp, top = 40.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewVariantHeader() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      VariantHeader()
    }
  }
}
