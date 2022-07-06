package com.hedvig.app.feature.offer.ui.composable.variants

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun VariantHeader() {
  Text(
    text = stringResource(id = R.string.offer_screen_varianted_offers_header),
    style = MaterialTheme.typography.h6,
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .padding(bottom = 4.dp, top = 40.dp),
  )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VariantHeaderPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      VariantHeader()
    }
  }
}
