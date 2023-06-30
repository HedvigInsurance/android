package com.hedvig.android.feature.odyssey.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import arrow.core.identity
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun <T> SingleSelectDialog(
  title: String,
  optionsList: List<T>,
  onSelected: (T) -> Unit,
  getDisplayText: (T) -> String,
  getId: (T) -> String,
  onDismissRequest: () -> Unit,
) {
  Dialog(onDismissRequest = { onDismissRequest.invoke() }) {
    SelectionContent(
      title = title,
      optionsList = optionsList,
      getId = getId,
      getDisplayText = getDisplayText,
      getIsSelected = null,
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
  getId: (T) -> String,
  onDismissRequest: () -> Unit,
) {
  Dialog(onDismissRequest = { onDismissRequest.invoke() }) {
    SelectionContent(
      title = title,
      optionsList = optionsList,
      getId = getId,
      getDisplayText = getDisplayText,
      getIsSelected = getIsSelected,
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
  onSelected: (T) -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    shape = MaterialTheme.shapes.squircle,
  ) {
    Column {
      Spacer(modifier = Modifier.height(32.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
      )
      Spacer(modifier = Modifier.height(32.dp))
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
      ) {
        items(
          items = optionsList,
          key = { option: T -> getId(option) },
          contentType = { "Option" },
        ) { option: T ->
          HedvigBigCard(
            onClick = { onSelected(option) },
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .heightIn(min = 72.dp)
                .fillMaxWidth()
                .padding(16.dp),
            ) {
              Text(
                text = getDisplayText(option),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
              )
              if (getIsSelected != null) {
                Spacer(Modifier.width(8.dp))
                Spacer(
                  Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .then(
                      if (getIsSelected.invoke(option) == true) {
                        Modifier.background(MaterialTheme.colorScheme.primary)
                      } else {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                      },
                    ),
                )
              }
            }
          }
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSelectionContent() {
  val selectedOptions = remember { mutableStateListOf("Front", "Water") }
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      SelectionContent(
        title = "Type of damage",
        optionsList = listOf("Front", "Back", "Water", "Other"),
        getId = ::identity,
        getDisplayText = ::identity,
        getIsSelected = { selectedOptions.contains(it) },
        onSelected = { selectedOptions.add(it) },
      )
    }
  }
}
