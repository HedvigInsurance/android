package com.hedvig.android.feature.home.claimdetail.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun ClaimType(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  @DrawableRes drawableRes: Int = hedvig.resources.R.drawable.ic_claim,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    Icon(
      painter = painterResource(drawableRes),
      contentDescription = null,
    )
    Column(
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.h6,
      )
      if (subtitle.isNotEmpty()) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.subtitle2,
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimType() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      Column {
        ClaimType(title = "title", subtitle = "subtitle")
        ClaimType(title = "title", subtitle = "")
      }
    }
  }
}
