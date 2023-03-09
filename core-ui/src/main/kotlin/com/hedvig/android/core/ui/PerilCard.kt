package com.hedvig.android.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader

@Composable
fun PerilCard(
  text: String,
  iconUrl: String,
  onClick: () -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier.requiredHeight(56.dp),
    elevation = HedvigCardElevation.Elevated(2.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(iconUrl)
          .build(),
        contentDescription = "Peril Icon",
        imageLoader = imageLoader,
        modifier = Modifier.size(24.dp),
      )
      Spacer(Modifier.width(8.dp))
      Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPerilCard() {
  HedvigTheme {
    PerilCard(
      "Fire",
      "",
      {},
      rememberPreviewImageLoader(),
      Modifier,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewPerilCardWithLongText() {
  HedvigTheme {
    PerilCard(
      List(20) { "Fire$it" }.joinToString(),
      "",
      {},
      rememberPreviewImageLoader(),
      Modifier,
    )
  }
}
