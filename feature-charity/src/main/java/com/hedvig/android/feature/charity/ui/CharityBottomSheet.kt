package com.hedvig.android.feature.charity.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.BottomSheetHandle
import hedvig.resources.R

@Composable
internal fun CharityBottomSheet(
  isShowing: Boolean,
  closeSheet: () -> Unit,
) {
  BackHandler(
    enabled = isShowing,
    onBack = closeSheet,
  )
  Column(
    Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
  ) {
    Spacer(Modifier.height(8.dp))
    BottomSheetHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
    Spacer(Modifier.height(8.dp))
    Column(
      Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
    ) {
      Spacer(Modifier.height(8.dp))
      Text(
        text = stringResource(R.string.CHARITY_INFO_DIALOG_TITLE),
        style = MaterialTheme.typography.subtitle1,
        modifier = Modifier.align(Alignment.CenterHorizontally),
      )
      Spacer(Modifier.height(24.dp))
      Text(
        text = stringResource(R.string.PROFILE_MY_CHARITY_INFO_BODY),
        style = MaterialTheme.typography.body2,
      )
      Spacer(Modifier.height(24.dp))
    }
  }
}

@Preview
@Composable
private fun CharityBottomSheetPreview() {
  CharityBottomSheet(true) {}
}
