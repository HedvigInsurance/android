package com.hedvig.android.feature.businessmodel

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.preview.calculateForPreview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun BusinessModelScreen(
  navigateUp: () -> Unit,
  windowSizeClass: WindowSizeClass,
) {
  val sheetState: ModalBottomSheetState = rememberModalBottomSheetState(
    initialValue = ModalBottomSheetValue.Hidden,
    skipHalfExpanded = true,
  )
  BusinessModelScreen(sheetState = sheetState, navigateUp = navigateUp, windowSizeClass = windowSizeClass)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BusinessModelScreen(
  sheetState: ModalBottomSheetState,
  navigateUp: () -> Unit,
  windowSizeClass: WindowSizeClass,
) {
  val coroutineScope = rememberCoroutineScope()
  ModalBottomSheetLayout(
    sheetState = sheetState,
    sheetContent = {
      BusinessModelBottomSheet(
        closeSheet = { coroutineScope.launch { sheetState.hide() } },
        isShowing = sheetState.isVisible,
      )
    },
    sheetShape = MaterialTheme.shapes.large.copy(bottomStart = CornerSize(0), bottomEnd = CornerSize(0)),
    modifier = Modifier.fillMaxSize(),
  ) {
    ScreenContent(
      showSheet = { coroutineScope.launch { sheetState.show() } },
      navigateUp = navigateUp,
      windowSizeClass = windowSizeClass,
    )
  }
}

@Composable
private fun ScreenContent(
  showSheet: () -> Unit,
  navigateUp: () -> Unit,
  windowSizeClass: WindowSizeClass,
  modifier: Modifier = Modifier,
) {
  Box(modifier.fillMaxSize()) {
    Column {
      TopAppBarWithBack(
        onClick = navigateUp,
        modifier = Modifier.windowInsetsPadding(
          WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        ),
      )
      Column(
        Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp)
          .windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
          ),
      ) {
        BusinessModelContent(
          openBusinessModelInfo = { showSheet() },
          windowSizeClass = windowSizeClass,
        )
      }
    }
  }
}

@Composable
private fun ColumnScope.BusinessModelContent(
  openBusinessModelInfo: () -> Unit,
  windowSizeClass: WindowSizeClass,
) {
  Text(
    text = stringResource(hedvig.resources.R.string.BUSINESS_MODEL_TITLE),
    style = MaterialTheme.typography.headlineLarge,
  )
  Spacer(Modifier.height(24.dp))
  BusinessModelImage(
    com.hedvig.android.feature.businessmodel.R.drawable.milkywire,
    windowSizeClass,
    Modifier.align(Alignment.CenterHorizontally),
  )
  Spacer(Modifier.height(24.dp))
  Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
      Text(
        text = stringResource(hedvig.resources.R.string.BUSINESS_MODEL_CARD_TITLE),
        style = MaterialTheme.typography.bodyLarge,
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.BUSINESS_MODEL_CARD_TEXT),
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
  Spacer(Modifier.height(8.dp))
  TextButton(
    onClick = {
      openBusinessModelInfo()
    },
    shape = MaterialTheme.shapes.squircle,
    modifier = Modifier.align(Alignment.CenterHorizontally),
  ) {
    Icon(
      imageVector = Icons.Outlined.Info,
      contentDescription = null,
    )
    Spacer(Modifier.width(8.dp))
    Text(
      stringResource(hedvig.resources.R.string.BUSINESS_MODEL_INFO_BUTTON_LABEL),
    )
  }
}

@Composable
private fun BusinessModelImage(
  @DrawableRes imageRes: Int,
  windowSizeClass: WindowSizeClass,
  modifier: Modifier = Modifier,
) {
  Image(
    painter = painterResource(imageRes),
    contentDescription = null,
    modifier = modifier
      .padding(horizontal = 24.dp)
      .fillMaxWidth(if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) 1f else 0.4f),
  )
}

@OptIn(ExperimentalMaterialApi::class)
@HedvigMultiScreenPreview
@Composable
private fun PreviewBusinessModelScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) showSheet: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      BusinessModelScreen(
        ModalBottomSheetState(if (showSheet) ModalBottomSheetValue.Expanded else ModalBottomSheetValue.Hidden),
        navigateUp = {},
        windowSizeClass = WindowSizeClass.calculateForPreview(),
      )
    }
  }
}
