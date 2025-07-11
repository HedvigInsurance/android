package com.hedvig.android.shared.file.upload.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Camera
import com.hedvig.android.design.system.hedvig.icon.Document
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Image
import hedvig.resources.R

@Composable
fun FilePickerBottomSheet(
  isVisible: Boolean,
  onPickPhoto: () -> Unit,
  onPickFile: () -> Unit,
  onTakePhoto: () -> Unit,
  onDismiss: () -> Unit,
) {
  HedvigBottomSheet(
    isVisible = isVisible,
    onVisibleChange = { visible ->
      if (!visible) {
        onDismiss()
      }
    },
    content = {
      FilePickerBottomSheetContent(
        onPickPhoto = onPickPhoto,
        onTakePhoto = onTakePhoto,
        onPickFile = onPickFile,
      )
    },
  )
}

@Composable
private fun FilePickerBottomSheetContent(
  onPickPhoto: () -> Unit,
  onTakePhoto: () -> Unit,
  onPickFile: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      ClickableOption(
        text = stringResource(R.string.file_upload_photo_library),
        icon = HedvigIcons.Image,
        onClick = onPickPhoto,
      )
      ClickableOption(
        text = stringResource(R.string.file_upload_take_photo),
        icon = HedvigIcons.Camera,
        onClick = onTakePhoto,
      )
      ClickableOption(
        text = stringResource(R.string.file_upload_choose_files),
        icon = HedvigIcons.Document,
        onClick = onPickFile,
      )
    }
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun ClickableOption(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .heightIn(56.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      HedvigText(
        text = text,
        style = HedvigTheme.typography.headlineMedium,
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(8.dp))
      Icon(imageVector = icon, contentDescription = EmptyContentDescription)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFilePickerBottomSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      FilePickerBottomSheetContent({}, {}, {})
    }
  }
}
