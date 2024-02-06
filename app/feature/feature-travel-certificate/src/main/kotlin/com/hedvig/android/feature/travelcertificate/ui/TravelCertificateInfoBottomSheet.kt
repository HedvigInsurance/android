package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import hedvig.resources.R

@Composable
internal fun TravelCertificateInfoBottomSheet(onDismiss: () -> Unit, sheetState: SheetState) {
  ModalBottomSheet(
    containerColor = MaterialTheme.colorScheme.background,
    onDismissRequest = {
      onDismiss()
    },
    shape = MaterialTheme.shapes.squircleLargeTop,
    sheetState = sheetState,
    tonalElevation = 0.dp,
  ) {
    Text(
      text = stringResource(id = R.string.travel_certificate_info_title),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = stringResource(id = R.string.travel_certificate_info_subtitle),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      onClick = { onDismiss() },
      modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
    )
  }
}
