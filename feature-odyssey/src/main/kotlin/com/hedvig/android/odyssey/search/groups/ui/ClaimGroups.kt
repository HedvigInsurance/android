package com.hedvig.android.odyssey.search.groups.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.odyssey.search.groups.ClaimGroup
import java.util.*

@Composable
internal fun ClaimGroups(
  selectGroup: (ClaimGroup) -> Unit,
  claimGroups: List<ClaimGroup>,
  imageLoader: ImageLoader,
) {
  claimGroups.map { claim ->
    Row(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clip(SquircleShape)
        .background(
          color = Color(0xFFF0F0F0),
          shape = SquircleShape,
        )
        .clickable {
          selectGroup(claim)
        }
        .padding(vertical = 20.dp, horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(claim.iconUrl)
          .crossfade(true)
          .build(),
        contentDescription = "Icon",
        imageLoader = imageLoader,
        modifier = Modifier.size(48.dp),
      )
      Spacer(modifier = Modifier.padding(12.dp))
      Text(
        text = claim.displayName,
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
      )
    }
    Spacer(modifier = Modifier.padding(4.dp))
  }
}

@HedvigPreview
@Composable
fun PreviewCommonClaims() {
  HedvigTheme {
    ClaimGroups(
      selectGroup = {},
      claimGroups = listOf(
        ClaimGroup(
          id = UUID.randomUUID().toString(),
          displayName = "Broken phone",
          iconUrl = ""
        ),
        ClaimGroup(
          id = UUID.randomUUID().toString(),
          displayName = "Stolen phone",
          iconUrl = "",
        ),
        ClaimGroup(
          id = UUID.randomUUID().toString(),
          displayName = "Broken computer",
          iconUrl = "",
        ),
        ClaimGroup(
          id = UUID.randomUUID().toString(),
          displayName = "Stolen computer",
          iconUrl = "",
        ),
        ClaimGroup(
          id = UUID.randomUUID().toString(),
          displayName = "Broken headphones",
          iconUrl = "",
        ),
        ClaimGroup(
          id = UUID.randomUUID().toString(),
          displayName = "Stolen headphones",
          iconUrl = "",
        ),
      ),
      imageLoader = rememberPreviewImageLoader()
    )
  }
}
