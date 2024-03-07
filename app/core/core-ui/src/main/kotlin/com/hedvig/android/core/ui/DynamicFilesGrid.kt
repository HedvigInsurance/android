package com.hedvig.android.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Document
import com.hedvig.android.core.icons.hedvig.normal.Pictures
import com.hedvig.android.core.icons.hedvig.normal.Play
import com.hedvig.android.core.icons.hedvig.normal.X
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.uidata.UiFile

/**
 * Makes a column with content above and below a dynamic lazy grid of files thumbnails. The column
 * takes the whole height, both the column and the inner grid are scrollable and the below content is always at the
 * bottom (as if the lazy grid was with weight(1f), but without predefined height) no matter what the size
 * of lazy grid and screen orientation are. Can be used for just the grid alone without above and below content too.
 */
@Composable
fun ColumnScope.DynamicFilesGridBetweenOtherThings(
  files: List<UiFile>,
  imageLoader: ImageLoader,
  onRemoveFile: ((fileId: String) -> Unit)?,
  onClickFile: ((fileId: String) -> Unit)?,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
  aboveGridContent: @Composable () -> Unit = {},
  belowGridContent: @Composable () -> Unit = {},
  // todo: added this as a separate argument bc it's easy to forget about spacing if we have no below content,
  // todo: and putting some spacing in the default belowGridContent also feels weird, bc this fun could be called from
  // todo: a simple Column, ClaimFlowScaffold etc.
) {
  var layoutHeight by remember { mutableIntStateOf(-1) }
  Layout(
    content = {
      Box {
        aboveGridContent()
      }
      Box {
        belowGridContent()
      }
      if (files.isNotEmpty()) {
        FilesLazyVerticalGrid(
          files = files,
          onRemoveFile = onRemoveFile,
          imageLoader = imageLoader,
          onClickFile = onClickFile,
        )
      }
    },
    modifier = modifier
      .fillMaxWidth()
      .weight(1f)
      .onSizeChanged { intSize -> layoutHeight = intSize.height }
      .verticalScroll(rememberScrollState()),
  ) { measurables, constraints ->
    val startContentPadding = contentPadding.calculateStartPadding(layoutDirection).roundToPx()
    val horizontalContentPadding = startContentPadding +
      contentPadding.calculateEndPadding(layoutDirection).roundToPx()
    val topContentPadding = contentPadding.calculateTopPadding().roundToPx()
    val bottomContentPadding = contentPadding.calculateBottomPadding().roundToPx()
    val beforeGridPlaceable = measurables[0].measure(
      constraints.copy(
        minWidth = 0,
        maxWidth = constraints.maxWidth - horizontalContentPadding,
        minHeight = 0,
      ),
    )
    val afterGridPlaceable = measurables[1].measure(
      constraints.copy(
        minWidth = 0,
        maxWidth = constraints.maxWidth - horizontalContentPadding,
        minHeight = 0,
      ),
    )
    val remainingHeightForGrid =
      layoutHeight - beforeGridPlaceable.height - afterGridPlaceable.height - topContentPadding - bottomContentPadding
    val actualGridHeight = remainingHeightForGrid.coerceAtLeast(170.dp.roundToPx())
    val gridPlaceable = measurables.getOrNull(2)?.measure(
      constraints.copy(
        minWidth = 0,
        minHeight = actualGridHeight,
        maxWidth = constraints.maxWidth - horizontalContentPadding,
        maxHeight = actualGridHeight,
      ),
    )
    val allContentMeasuredHeight =
      beforeGridPlaceable.height + afterGridPlaceable.height + (gridPlaceable?.height ?: 0)
    layout(
      width = constraints.maxWidth,
      height = (allContentMeasuredHeight + topContentPadding + bottomContentPadding).coerceAtLeast(layoutHeight),
    ) {
      beforeGridPlaceable.place(startContentPadding, topContentPadding)
      gridPlaceable?.place(startContentPadding, beforeGridPlaceable.height + topContentPadding)
      if (gridPlaceable == null && remainingHeightForGrid > 0) {
        afterGridPlaceable.place(startContentPadding, layoutHeight - (afterGridPlaceable.height + bottomContentPadding))
      } else {
        afterGridPlaceable.place(
          startContentPadding,
          beforeGridPlaceable.height + (gridPlaceable?.height ?: 0) + topContentPadding,
        )
      }
    }
  }
}

/**
 * Note that the [paddingValues] are added on top of a predefined `PaddingValues(top = 8.dp, end = 8.dp, start = 8.dp)`
 * which is used to ensure that the offset X button does not get clipped, nor does its shadow
 */
@Composable
private fun FilesLazyVerticalGrid(
  files: List<UiFile>,
  onRemoveFile: ((fileId: String) -> Unit)?,
  onClickFile: ((fileId: String) -> Unit)?,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  paddingValues: PaddingValues = PaddingValues(),
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(109.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(top = 8.dp) + paddingValues,
    // todo: changed this one here (see doc above), left only PaddingValues(top = 8.dp). Seems that the standard horizontal padding
// todo: is enough for offset button either way, and it's closer to design this way. Let me know what you think
    modifier = modifier,
  ) {
    items(
      items = files,
      key = { it.id },
    ) { uiFile ->
      File(
        id = uiFile.id,
        name = uiFile.name,
        path = uiFile.localPath ?: uiFile.url,
        mimeType = uiFile.mimeType,
        imageLoader = imageLoader,
        onRemoveFile = onRemoveFile,
        onClickFile = onClickFile,
      )
    }
  }
}

@Composable
private fun File(
  id: String,
  name: String,
  path: String?,
  mimeType: String,
  imageLoader: ImageLoader,
  onRemoveFile: ((fileId: String) -> Unit)?,
  onClickFile: ((fileId: String) -> Unit)?,
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
        .clip(MaterialTheme.shapes.squircleMedium)
        .clickable {
          if (onClickFile != null) {
            onClickFile(id)
          }
        },
      propagateMinConstraints = true,
    ) {
      if (mimeType.contains("image") && path != null) {
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
    if (onRemoveFile != null) {
      IconButton(
        onClick = { onRemoveFile(id) },
        colors = IconButtonDefaults.iconButtonColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier
          .size(16.dp)
          .wrapContentSize(unbounded = true)
          .minimumInteractiveComponentSize()
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
}

private fun getIconFromMimeType(mimeType: String) = when (mimeType) {
  "image/jpg" -> Icons.Hedvig.Pictures
  "video/quicktime" -> Icons.Hedvig.Play
  "application/pdf" -> Icons.Hedvig.Document
  else -> Icons.Hedvig.Document
}

@HedvigPreview
@Composable
private fun PreviewDynamicFilesGridBetweenOtherThings() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        DynamicFilesGridBetweenOtherThings(
          aboveGridContent = {
            Column {
              Spacer(Modifier.height(16.dp))
              Text("IMPORTANT TEXT AT THE TOP", Modifier.padding(horizontal = 16.dp))
              Spacer(Modifier.height(8.dp))
            }
          },
          belowGridContent = {
            Column {
              Spacer(Modifier.height(16.dp))
              Text("IMPORTANT TEXT AT THE BOTTOM", Modifier.padding(horizontal = 16.dp))
            }
          },
          files = listOf(
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "image/jpg",
              url = null,
              thumbnailUrl = null,
              id = "1",
            ),
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "application/pdf",
              url = null,
              thumbnailUrl = null,
              id = "2",
            ),
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "video/quicktime",
              url = null,
              thumbnailUrl = null,
              id = "3",
            ),
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "other",
              url = null,
              thumbnailUrl = null,
              id = "4",
            ),
          ),
          imageLoader = rememberPreviewImageLoader(),
          onRemoveFile = null,
          onClickFile = null,
          modifier = Modifier,
          contentPadding = PaddingValues(16.dp),
        )
      }
    }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewDynamicFilesGridManyFiles() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        DynamicFilesGridBetweenOtherThings(
          aboveGridContent = {
            Column {
              Spacer(Modifier.height(16.dp))
              Text("IMPORTANT TEXT AT THE TOP", Modifier.padding(horizontal = 16.dp))
              Spacer(Modifier.height(8.dp))
            }
          },
          belowGridContent = {
            Column {
              List(20) {
                Spacer(Modifier.height(16.dp))
                Text("IMPORTANT TEXT AT THE BOTTOM", Modifier.padding(horizontal = 16.dp))
              }
            }
          },
          files = List(25) {
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "image/jpg",
              url = null,
              thumbnailUrl = null,
              id = "$it",
            )
          },
          imageLoader = rememberPreviewImageLoader(),
          onRemoveFile = null,
          onClickFile = null,
          modifier = Modifier,
          contentPadding = PaddingValues(16.dp),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFile() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        FilesLazyVerticalGrid(
          files = listOf(
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "image/jpg",
              url = null,
              thumbnailUrl = null,
              id = "1",
            ),
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "application/pdf",
              url = null,
              thumbnailUrl = null,
              id = "2",
            ),
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "video/quicktime",
              url = null,
              thumbnailUrl = null,
              id = "3",
            ),
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "other",
              url = null,
              thumbnailUrl = null,
              id = "4",
            ),
          ),
          onRemoveFile = {},
          onClickFile = null,
          imageLoader = rememberPreviewImageLoader(),
          paddingValues =
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
              .asPaddingValues(),
        )
      }
    }
  }
}
