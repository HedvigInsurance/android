package com.hedvig.android.feature.odyssey.step.fileupload

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.HedvigIcons
import com.hedvig.android.core.icons.hedvig.normal.Document
import com.hedvig.android.core.icons.hedvig.normal.Pictures
import com.hedvig.android.core.icons.hedvig.normal.Play
import com.hedvig.android.core.icons.hedvig.normal.X
import com.hedvig.android.core.ui.FileContainer
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.data.claimflow.LocalFile
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun AddFilesScreen(
  uiState: FileUploadUiState,
  windowSizeClass: WindowSizeClass,
  onContinue: () -> Unit,
  onAddMoreFiles: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
  onRemoveFile: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.errorMessage != null,
      showedError = showedError,
    ),
    scrollable = false,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        .asPaddingValues(),
    ) {
      items(uiState.uploadedFiles) {
        File(
          id = it.id,
          name = it.name,
          path = it.path,
          mimeType = it.mimeType,
          imageLoader = imageLoader,
          onRemoveFile = onRemoveFile
        )
      }
      items(uiState.localFiles) {
        File(
          id = it.id,
          name = it.id,
          path = it.path,
          mimeType = it.mimeType,
          imageLoader = imageLoader,
          onRemoveFile = onRemoveFile
        )
      }
    }
    Spacer(Modifier.weight(1f))
    HedvigSecondaryContainedButton(
      text = stringResource(R.string.claim_status_detail_add_more_files),
      onClick = onAddMoreFiles,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(8.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_continue_button),
      onClick = onContinue,
      isLoading = uiState.isLoading,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
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
  Box(contentAlignment = Alignment.TopEnd) {
    Box(
      Modifier
        .padding(5.dp)
        .background(
          shape = MaterialTheme.shapes.squircleMedium,
          color = MaterialTheme.colorScheme.surface,
        )
        .height(109.dp),
      contentAlignment = Alignment.Center,
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
          modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
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
      onClick = {
        onRemoveFile(id)
      },
      modifier = Modifier
        .shadow(elevation = 3.dp, shape = CircleShape)
        .background(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surface,
        )
        .size(24.dp),
    ) {
      Icon(
        imageVector = Icons.Hedvig.X,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun AddFilesScreenPreview() {
  HedvigTheme {
    Surface {
      AddFilesScreen(
        uiState = FileUploadUiState(
          localFiles = listOf(
            LocalFile(
              "",
              "",
              "123123123123123123123",
            ),
            LocalFile(
              "",
              "",
              "1234",
            ),
            LocalFile(
              "",
              "",
              "1232",
            ),
            LocalFile(
              "",
              "",
              "1123",
            ),
          ),
        ),
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        onContinue = {},
        onAddMoreFiles = {},
        showedError = {},
        navigateUp = {},
        closeClaimFlow = {},
        onRemoveFile = {},
        imageLoader = rememberPreviewImageLoader(),
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
