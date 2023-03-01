package com.hedvig.android.odyssey.input.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> SingleSelectDialog(
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
    Surface(
      modifier = Modifier.width(300.dp),
      shape = RoundedCornerShape(10.dp),
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(text = title, Modifier.padding(12.dp))

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
          items(
            items = optionsList,
            key = { option: T -> getId(option) },
            contentType = { "Option" },
          ) { option: T ->
            ListItem(
              text = { Text(text = getDisplayText(option)) },
              icon = if (getImageUrl(option) != null) {
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
              singleLineSecondaryText = true,
              modifier = Modifier.clickable {
                onSelected(option)
                onDismissRequest()
              },
            )
          }
        }
      }
    }
  }
}
