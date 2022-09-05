package com.hedvig.android.feature.businessmodel.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import com.hedvig.android.core.designsystem.theme.textColorLink
import com.hedvig.android.core.ui.FullScreenHedvigProgress
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.feature.businessmodel.BusinessModelInformation
import com.hedvig.android.feature.businessmodel.BusinessModelUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun BusinessModelScreen(
  uiState: BusinessModelUiState,
  retry: () -> Unit,
  goBack: () -> Unit,
  imageLoader: ImageLoader,
  windowSizeClass: WindowSizeClass,
) {
  val coroutineScope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(
    initialValue = ModalBottomSheetValue.Hidden,
    skipHalfExpanded = true,
  )
  ModalBottomSheetLayout(
    sheetState = sheetState,
    sheetContent = {
      BusinessModelBottomSheet(
        closeSheet = { coroutineScope.launch { sheetState.hide() } },
        isShowing = sheetState.isVisible,
      )
    },
    modifier = Modifier.fillMaxSize(),
  ) {
    ScreenContent(
      uiState = uiState,
      showSheet = { coroutineScope.launch { sheetState.show() } },
      goBack = goBack,
      retry = retry,
      imageLoader = imageLoader,
      windowSizeClass = windowSizeClass,
    )
  }
}

@Composable
private fun ScreenContent(
  uiState: BusinessModelUiState,
  showSheet: () -> Unit,
  goBack: () -> Unit,
  retry: () -> Unit,
  imageLoader: ImageLoader,
  windowSizeClass: WindowSizeClass,
) {
  Box(Modifier.fillMaxSize()) {
    Column {
      TopAppBarWithBack(
        onClick = goBack,
        modifier = Modifier.windowInsetsPadding(
          WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        ),
        title = "",
      )
      Column(
        Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp)
          .windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
          ),
      ) {
        val businessModelInfo = uiState.businessModelInformation
        if (businessModelInfo != null) {
          SuccessContent(
            businessModelInfo = businessModelInfo,
            openBusinessModelInfo = { showSheet() },
            imageLoader = imageLoader,
            windowSizeClass = windowSizeClass,
          )
        } else if (uiState.isLoading.not()) {
          GenericErrorScreen(
            onRetryButtonClick = { retry() },
            Modifier.padding(top = 110.dp),
          )
        }
      }
    }
    FullScreenHedvigProgress(uiState.isLoading)
  }
}

@Composable
private fun ColumnScope.SuccessContent(
  businessModelInfo: BusinessModelInformation,
  openBusinessModelInfo: () -> Unit,
  imageLoader: ImageLoader,
  windowSizeClass: WindowSizeClass,
) {
  Text(
    text = stringResource(hedvig.resources.R.string.BUSINESS_MODEL_TITLE),
    style = MaterialTheme.typography.h4,
  )
  if (businessModelInfo.imageUrl != null) {
    BusinessModelImage(
      com.hedvig.android.feature.businessmodel.R.drawable.milkywire,
      imageLoader,
      windowSizeClass,
      Modifier.align(Alignment.CenterHorizontally),
    )
  }
  Spacer(Modifier.height(24.dp))
  Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
      Text(
        text = stringResource(hedvig.resources.R.string.BUSINESS_MODEL_CARD_TITLE),
        style = MaterialTheme.typography.subtitle1,
      )
      if (businessModelInfo.description != null) {
        Spacer(Modifier.height(8.dp))
        Text(
          text = stringResource(hedvig.resources.R.string.BUSINESS_MODEL_CARD_TEXT),
          style = MaterialTheme.typography.body2,
        )
      }
    }
  }
  Spacer(Modifier.height(8.dp))
  TextButton(
    onClick = {
      openBusinessModelInfo()
    },
    colors = ButtonDefaults.textButtonColors(
      contentColor = MaterialTheme.colors.textColorLink,
      disabledContentColor = MaterialTheme.colors.textColorLink.copy(alpha = 0.12f),
    ),
    modifier = Modifier.align(Alignment.CenterHorizontally),
  ) {
    Icon(
      imageVector = Icons.Outlined.Info,
      contentDescription = null,
      tint = MaterialTheme.colors.textColorLink,
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
  imageLoader: ImageLoader,
  windowSizeClass: WindowSizeClass,
  modifier: Modifier = Modifier,
) {
  val painter = rememberAsyncImagePainter(
    model = ImageRequest.Builder(LocalContext.current)
      .data(imageRes)
      .size(Size.ORIGINAL)
      .build(),
    imageLoader = imageLoader,
  )
  Box(
    modifier
      .fillMaxWidth(if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) 1f else 0.4f)
      .animateContentSize(),
  ) {
    when (painter.state) {
      AsyncImagePainter.State.Empty -> {}
      is AsyncImagePainter.State.Error -> {}
      is AsyncImagePainter.State.Loading -> {
        Column {
          Spacer(Modifier.height(24.dp))
          Box(
            Modifier
              .fillMaxWidth()
              .height(50.dp)
              .placeholder(
                visible = true,
                highlight = PlaceholderHighlight.fade(),
              ),
          )
        }
      }
      is AsyncImagePainter.State.Success -> {
        Column {
          Spacer(Modifier.height(24.dp))
          Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.padding(horizontal = 24.dp),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun BusinessModelScreenPreview() {
  BusinessModelScreen(
    uiState = BusinessModelUiState(
      businessModelInformation = BusinessModelInformation(
        name = "Name of partner",
        description = "Some long description maybe? Yes. ".repeat(8),
        imageUrl = null,
      ),
      isLoading = false,
    ),
    retry = {},
    goBack = {},
    imageLoader = rememberPreviewImageLoader(),
    windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(500.dp, 300.dp)),
  )
}
