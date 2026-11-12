package com.hedvig.android.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.HedvigSnackBar
import com.hedvig.android.design.system.hedvig.TopAppBarDefaults

@Composable
internal fun GlobalHedvigSnackBar(globalSnackBarState: GlobalSnackBarState) {
  HedvigSnackBar(
    globalSnackBarState = globalSnackBarState,
    modifier = Modifier
      .wrapContentHeight(Alignment.Top, unbounded = true)
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(top = TopAppBarDefaults.containerHeight)
      .padding(16.dp)
      .zIndex(1f),
  )
}
