package com.hedvig.android.feature.charity.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.placeholder
import com.hedvig.android.core.designsystem.theme.textColorLink
import com.hedvig.android.core.ui.FullScreenHedvigProgress
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.feature.charity.CharityInformation
import com.hedvig.android.feature.charity.CharityUiState
import hedvig.resources.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun CharityScreen(
  uiState: CharityUiState,
  retry: () -> Unit,
  goBack: () -> Unit,
  imageLoader: ImageLoader,
) {
  val coroutineScope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(
    initialValue = ModalBottomSheetValue.Hidden,
    skipHalfExpanded = true,
  )
  ModalBottomSheetLayout(
    sheetState = sheetState,
    sheetContent = {
      CharityBottomSheet(
        isShowing = sheetState.isVisible,
        closeSheet = { coroutineScope.launch { sheetState.hide() } },
      )
    },
    modifier = Modifier.fillMaxSize(),
  ) {
    Box(Modifier.fillMaxSize()) {
      Column {
        TopAppBarWithBack(
          onClick = { goBack() },
          title = "",
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
          val charityInfo = uiState.charityInformation
          if (charityInfo != null) {
            CharityItems(
              charityInfo = charityInfo,
              openCharityInfo = {
                coroutineScope.launch { sheetState.show() }
              },
              imageLoader = imageLoader,
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
}

@Composable
private fun ColumnScope.CharityItems(
  charityInfo: CharityInformation,
  openCharityInfo: () -> Unit,
  imageLoader: ImageLoader,
) {
  Text(
    text = stringResource(R.string.PROFILE_CHARITY_TITLE),
    style = MaterialTheme.typography.h4,
  )
  if (charityInfo.imageUrl != null) {
    CharityImage(charityInfo.imageUrl, imageLoader, Modifier.align(Alignment.CenterHorizontally))
  }
  Spacer(Modifier.height(48.dp))
  Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
      Text(
        text = charityInfo.name,
        style = MaterialTheme.typography.subtitle1,
      )
      if (charityInfo.description != null) {
        Spacer(Modifier.height(8.dp))
        Text(
          text = charityInfo.description,
          style = MaterialTheme.typography.body2,
        )
      }
    }
  }
  Spacer(Modifier.height(8.dp))
  TextButton(
    onClick = {
      openCharityInfo()
    },
    colors = ButtonDefaults.textButtonColors(
      contentColor = MaterialTheme.colors.textColorLink,
      disabledContentColor = MaterialTheme.colors.textColorLink.copy(alpha = 0.12f),
    ),
    modifier = Modifier.align(Alignment.CenterHorizontally),
  ) {
    Icon(
      imageVector = Icons.Outlined.Info,
      contentDescription = Icons.Outlined.Info.name,
      tint = MaterialTheme.colors.textColorLink,
    )
    Spacer(Modifier.width(8.dp))
    Text(
      stringResource(R.string.CHARITY_INFO_BUTTON_LABEL),
    )
  }
}

@Composable
private fun ColumnScope.CharityImage(
  imageUrl: String,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  var isLoading by remember { mutableStateOf(false) }
  var isFailed by remember { mutableStateOf(false) }
  AnimatedVisibility(!isFailed) {
    Spacer(Modifier.height(48.dp))
  }
  SubcomposeAsyncImage(
    model = imageUrl.repeat(0),
    contentDescription = null,
    imageLoader = imageLoader,
    loading = {
      Box(
        Modifier
          .fillMaxWidth()
          .height(200.dp)
          .placeholder(
            visible = true,
            highlight = PlaceholderHighlight.fade(),
          ),
      )
    },
    success = {
      SubcomposeAsyncImageContent(modifier.padding(horizontal = 24.dp))
    },
    error = {},
    onLoading = {},
    onSuccess = {},
    onError = {},
    contentScale = ContentScale.Fit,
    modifier = modifier
      .fillMaxWidth()
      .placeholder(
        visible = isLoading,
        highlight = PlaceholderHighlight.fade(),
      )
      .animateContentSize(),
  )
}

@Preview
@Composable
private fun CharityScreenPreview() {
  CharityScreen(
    uiState = CharityUiState(
      charityInformation = CharityInformation(
        name = "Name of charity",
        description = "Some long description maybe? Yes.".repeat(8),
        imageUrl = null,
      ),
      isLoading = false,
    ),
    retry = {},
    goBack = {},
    imageLoader = rememberPreviewImageLoader(),
  )
}
