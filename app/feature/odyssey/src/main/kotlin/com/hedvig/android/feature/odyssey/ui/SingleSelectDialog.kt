package com.hedvig.android.feature.odyssey.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader

@Composable
internal fun <T> SingleSelectDialog(
  title: String,
  optionsList: List<T>,
  onSelected: (T) -> Unit,
  getDisplayText: (T) -> String,
  getImageUrl: (T) -> String?,
  getId: (T) -> String,
  imageLoader: ImageLoader,
  onDismissRequest: () -> Unit,
) {
  Dialog(onDismissRequest = { onDismissRequest.invoke() }) {
    SelectionContent(
      title = title,
      optionsList = optionsList,
      getId = getId,
      getDisplayText = getDisplayText,
      getIsSelected = null,
      getImageUrl = getImageUrl,
      imageLoader = imageLoader,
      onSelected = {
        onDismissRequest()
        onSelected(it)
      },
    )
  }
}

@Composable
internal fun <T> MultiSelectDialog(
  title: String,
  optionsList: List<T>,
  onSelected: (T) -> Unit,
  getDisplayText: (T) -> String,
  getIsSelected: (T) -> Boolean,
  getImageUrl: (T) -> String?,
  getId: (T) -> String,
  imageLoader: ImageLoader,
  onDismissRequest: () -> Unit,
) {
  Dialog(onDismissRequest = { onDismissRequest.invoke() }) {
    SelectionContent(
      title = title,
      optionsList = optionsList,
      getId = getId,
      getDisplayText = getDisplayText,
      getIsSelected = getIsSelected,
      getImageUrl = getImageUrl,
      imageLoader = imageLoader,
      onSelected = onSelected,
    )
  }
}

@Composable
private fun <T> SelectionContent(
  title: String,
  optionsList: List<T>,
  getId: (T) -> String,
  getDisplayText: (T) -> String,
  getIsSelected: ((T) -> Boolean)?,
  getImageUrl: (T) -> String?,
  imageLoader: ImageLoader,
  onSelected: (T) -> Unit,
) {
  Surface(shape = MaterialTheme.shapes.large) {
    Column {
      Spacer(modifier = Modifier.height(24.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 24.dp),
      )
      Spacer(modifier = Modifier.height(12.dp))
      LazyColumn(
        contentPadding = PaddingValues(bottom = 12.dp),
      ) {
        items(
          items = optionsList,
          key = { option: T -> getId(option) },
          contentType = { "Option" },
        ) { option: T ->
          ListItem(
            headlineContent = { Text(text = getDisplayText(option)) },
            leadingContent = if (getImageUrl(option) != null) {
              {
                AsyncImage(
                  model = ImageRequest.Builder(LocalContext.current)
                    .data(getImageUrl(option))
                    .crossfade(true)
                    .build(),
                  contentDescription = "Icon",
                  imageLoader = imageLoader,
                  modifier = Modifier
                    .width(20.dp)
                    .height(34.dp),
                )
              }
            } else {
              null
            },
            trailingContent = {
              if (getIsSelected?.invoke(option) == true) {
                Icon(Icons.Default.Check, null)
              }
            },
            modifier = Modifier
              .clickable { onSelected(option) }
              .padding(horizontal = 12.dp),
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSelectionContent() {
  val selectedOptions = remember { mutableStateListOf(0, 5) }
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SelectionContent(
        title = "Title",
        optionsList = List(10) { it },
        getId = Int::toString,
        getDisplayText = Int::toString,
        getIsSelected = { selectedOptions.contains(it) },
        getImageUrl = { null },
        imageLoader = rememberPreviewImageLoader(),
        onSelected = { selectedOptions.add(it) },
      )
    }
  }
}
