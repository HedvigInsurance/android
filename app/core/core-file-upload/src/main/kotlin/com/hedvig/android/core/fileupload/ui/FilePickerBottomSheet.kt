package com.hedvig.android.core.fileupload.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.bottomsheet.HedvigBottomSheet
import com.hedvig.android.core.designsystem.component.button.ClickableOption
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
      Column(
        modifier = Modifier.padding(horizontal = 16.dp),
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
    },
  )
}
