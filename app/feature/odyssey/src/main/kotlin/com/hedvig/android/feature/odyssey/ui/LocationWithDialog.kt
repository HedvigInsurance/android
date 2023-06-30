package com.hedvig.android.feature.odyssey.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.data.claimflow.LocationOption
import hedvig.resources.R

@Composable
internal fun LocationWithDialog(
  locationOptions: List<LocationOption>,
  selectedLocation: LocationOption?,
  selectLocationOption: (LocationOption) -> Unit,
  enabled: Boolean,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  var showLocationPickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showLocationPickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.claims_location_screen_title),
      optionsList = locationOptions,
      onSelected = selectLocationOption,
      getDisplayText = { it.displayName },
      getImageUrl = { null },
      getId = { it.displayName },
      imageLoader = imageLoader,
      onDismissRequest = { showLocationPickerDialog = false },
    )
  }

  HedvigBigCard(
    onClick = { showLocationPickerDialog = true },
    hintText = stringResource(R.string.claims_location_screen_title),
    inputText = selectedLocation?.displayName,
    enabled = enabled,
    modifier = modifier,
  )
}

@HedvigPreview
@Composable
private fun PreviewLocationWithDialog(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasSelectedLocation: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      LocationWithDialog(
        emptyList(),
        if (hasSelectedLocation) LocationOption("", "Stockholm") else null,
        {},
        false,
        rememberPreviewImageLoader(),
      )
    }
  }
}
