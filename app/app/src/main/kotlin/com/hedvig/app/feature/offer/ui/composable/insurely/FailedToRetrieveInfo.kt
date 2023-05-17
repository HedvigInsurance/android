package com.hedvig.app.feature.offer.ui.composable.insurely

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun FailedToRetrieveInfo(insuranceProviderDisplayName: String?) {
  Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.padding(
      start = 16.dp,
      top = 20.dp,
      end = 16.dp,
      bottom = 16.dp,
    ),
  ) {
    Icon(painterResource(com.hedvig.android.core.designsystem.R.drawable.ic_warning_triangle), null)
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = if (insuranceProviderDisplayName != null) {
          stringResource(hedvig.resources.R.string.offer_screen_insurely_error_title, insuranceProviderDisplayName)
        } else {
          stringResource(hedvig.resources.R.string.offer_screen_insurely_multiple_error_title)
        },
        style = MaterialTheme.typography.subtitle1,
      )
      CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
          text = if (insuranceProviderDisplayName != null) {
            stringResource(
              hedvig.resources.R.string.offer_screen_insurely_error_description,
              insuranceProviderDisplayName,
            )
          } else {
            stringResource(hedvig.resources.R.string.offer_screen_insurely_multiple_error_description)
          },
          style = MaterialTheme.typography.body2,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFailedToRetrieveInfo() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      Column {
        FailedToRetrieveInfo(null)
        FailedToRetrieveInfo("FakeInsuranceProvider")
      }
    }
  }
}
