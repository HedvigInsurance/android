package com.hedvig.android.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.HedvigIcons
import com.hedvig.android.core.icons.hedvig.normal.Document
import com.hedvig.android.core.icons.hedvig.normal.Pictures
import com.hedvig.android.core.icons.hedvig.normal.Play
import com.hedvig.android.core.icons.hedvig.normal.X
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.uidata.UiFile

/**
 * Note that the [paddingValues] are added on top of a predefined `PaddingValues(top = 8.dp, end = 8.dp, start = 8.dp)`
 * which is used to ensure that the offset X button does not get clipped, nor does its shadow
 */
@Composable
fun FilesLazyVerticalGrid(
  files: List<UiFile>,
  onRemoveFile: (fileId: String) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  paddingValues: PaddingValues = PaddingValues(0.dp),
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(109.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(top = 8.dp, end = 8.dp, start = 8.dp) + paddingValues,
    modifier = modifier,
  ) {
    items(
      items = files,
      key = { it.id },
    ) { uiFile ->
      File(
        id = uiFile.id,
        name = uiFile.name,
        path = uiFile.path,
        mimeType = uiFile.mimeType,
        imageLoader = imageLoader,
        onRemoveFile = onRemoveFile,
      )
    }
  }
}

@Composable
private fun File(
  id: String,
  name: String,
  path: String,
  mimeType: String,
  imageLoader: ImageLoader,
  onRemoveFile: (String) -> Unit,
) {
  Box(
    contentAlignment = Alignment.TopEnd,
    modifier = Modifier
      .fillMaxSize()
      .aspectRatio(1f),
  ) {
    Box(
      modifier = Modifier
        .matchParentSize()
        .background(
          shape = MaterialTheme.shapes.squircleMedium,
          color = MaterialTheme.colorScheme.surface,
        )
        .clip(MaterialTheme.shapes.squircleMedium),
      propagateMinConstraints = true,
    ) {
      if (mimeType.contains("image")) {
        FileContainer(
          model = path,
          imageLoader = imageLoader,
          cacheKey = id,
        )
      } else {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(16.dp),
        ) {
          Icon(
            imageVector = getIconFromMimeType(mimeType),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "content icon",
          )
          Text(
            text = name,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }
    IconButton(
      onClick = { onRemoveFile(id) },
      colors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
      modifier = Modifier
        .size(16.dp)
        .wrapContentSize(unbounded = true)
        .shadow(elevation = 4.dp, shape = CircleShape)
        .size(24.dp),
    ) {
      Icon(
        imageVector = Icons.Hedvig.X,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

private fun getIconFromMimeType(mimeType: String) = when (mimeType) {
  "image/jpg" -> HedvigIcons.Pictures
  "video/quicktime" -> HedvigIcons.Play
  "application/pdf" -> HedvigIcons.Document
  else -> HedvigIcons.Document
}

@HedvigPreview
@Composable
private fun PreviewFile() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        FilesLazyVerticalGrid(
          files = listOf(
            UiFile("file", "path", "image/jpg", "1"),
            UiFile("file", "path", "video/quicktime", "2"),
            UiFile("file", "path", "application/pdf", "3"),
            UiFile("file", "path", "other", "4"),
          ),
          {},
          rememberPreviewImageLoader(),
        )
      }
    }
  }
}
