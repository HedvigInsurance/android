package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.File

@Composable
internal fun FilesRow(
  uiFiles: List<UiFile>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onRemoveFile: ((fileId: String) -> Unit)?,
  alignment: Alignment.Horizontal,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  modifier: Modifier = Modifier,
) {
  LazyRow(
    modifier
      .fillMaxWidth()
      .height(120.dp),
    contentPadding = contentPadding,
    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment),
  ) {
    items(
      items = uiFiles,
      key = { it.id },
    ) { uiFile ->
      File(
        id = uiFile.id,
        name = uiFile.name,
        path = uiFile.localPath ?: uiFile.url,
        mimeType = uiFile.mimeType,
        imageLoader = imageLoader,
        onRemoveFile = onRemoveFile,
        onClickFile = {
          onNavigateToImageViewer(it, it)
        },
        onNavigateToImageViewer = onNavigateToImageViewer,
      )
    }
  }
}
