package com.hedvig.android.core.fileupload.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.bottomsheet.HedvigBottomSheet
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Camera
import com.hedvig.android.core.icons.hedvig.normal.Document
import com.hedvig.android.core.icons.hedvig.normal.Pictures
import hedvig.resources.R

@Composable
fun FilePickerBottomSheet(
  sheetState: SheetState = rememberModalBottomSheetState(),
  onPickPhoto: () -> Unit,
  onPickFile: () -> Unit,
  onTakePhoto: () -> Unit,
  onDismiss: () -> Unit,
) {
  HedvigBottomSheet(
    onDismissed = onDismiss,
    sheetState = sheetState,
    content = {
      FilePickerBottomSheetContent(
        onPickPhoto = onPickPhoto,
        onTakePhoto = onTakePhoto,
        onPickFile = onPickFile,
        modifier = Modifier.padding(horizontal = 16.dp),
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
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    ClickableOption(
      text = stringResource(R.string.file_upload_photo_library),
      icon = Icons.Hedvig.Pictures,
      onClick = onPickPhoto,
    )
    ClickableOption(
      text = stringResource(R.string.file_upload_take_photo),
      icon = Icons.Hedvig.Camera,
      onClick = onTakePhoto,
    )
    ClickableOption(
      text = stringResource(R.string.file_upload_choose_files),
      icon = Icons.Hedvig.Document,
      onClick = onPickFile,
    )
  }
}

@Composable
private fun ClickableOption(
  text: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  cardColors: CardColors = CardDefaults.outlinedCardColors(),
) {
  HedvigCard(
    onClick = onClick,
    colors = cardColors,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .heightIn(56.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(8.dp))
      Icon(imageVector = icon, contentDescription = null)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFilePickerBottomSheetContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      FilePickerBottomSheetContent({}, {}, {})
    }
  }
}
