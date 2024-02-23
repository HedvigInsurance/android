package com.hedvig.android.core.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import arrow.core.identity
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.core.ui.preview.DoubleBooleanCollectionPreviewParameterProvider

@Composable
fun <T> SingleSelectDialog(
  title: String,
  optionsList: List<T>,
  onSelected: (T) -> Unit,
  getDisplayText: (T) -> String,
  getIsSelected: ((T) -> Boolean)?,
  getId: (T) -> String,
  onDismissRequest: () -> Unit,
  // True will not force the bigger min height that most of the design system has.
  smallSelectionItems: Boolean = false,
) {
  Dialog(onDismissRequest = { onDismissRequest.invoke() }) {
    SelectionContent(
      title = title,
      optionsList = optionsList,
      getId = getId,
      getDisplayText = getDisplayText,
      getIsSelected = getIsSelected,
      onSelected = {
        onDismissRequest()
        onSelected(it)
      },
      smallSelectionItems = smallSelectionItems,
    )
  }
}

@Composable
fun <T> MultiSelectDialog(
  title: String,
  optionsList: List<T>,
  onSelected: (T) -> Unit,
  getDisplayText: (T) -> String,
  getIsSelected: ((T) -> Boolean)?,
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
  smallSelectionItems: Boolean = false,
  lazyListState: LazyListState = rememberLazyListState(),
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    shape = MaterialTheme.shapes.squircleMedium,
  ) {
    Column {
      Spacer(modifier = Modifier.height(32.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
      )
      Spacer(Modifier.height((if (smallSelectionItems) 32 else 16).dp))
      val density = LocalDensity.current
      val lazyColumnContentPadding = 16.dp
      val showTopBorder by remember {
        derivedStateOf {
          lazyListState.firstVisibleItemIndex != 0 ||
            (lazyListState.firstVisibleItemScrollOffset > with(density) { lazyColumnContentPadding.roundToPx() })
        }
      }
      if (showTopBorder) {
        Box(
          Modifier.fillMaxWidth().height(0.dp)
            .wrapContentHeight(align = Alignment.Bottom, unbounded = true),
        ) {
          HorizontalDivider()
        }
      }
      LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        contentPadding = PaddingValues(lazyColumnContentPadding),
      ) {
        items(
          items = optionsList,
          key = { option: T -> getId(option) },
          contentType = { "Option" },
        ) { option: T ->
          HedvigCard(
            onClick = { onSelected(option) },
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .then(
                  if (smallSelectionItems) {
                    Modifier
                  } else {
                    Modifier.heightIn(72.dp)
                  },
                )
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
              Text(
                text = getDisplayText(option),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
              )
              if (getIsSelected != null) {
                Spacer(Modifier.width(8.dp))
                SelectIndicationCircle(getIsSelected(option))
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
private fun PreviewSelectionContent(
  @PreviewParameter(DoubleBooleanCollectionPreviewParameterProvider::class) input: Pair<Boolean, Boolean>,
) {
  val (isScrolled, smallSelectionItems) = input
  val selectedOptions = remember { mutableStateListOf("Front", "Water") }
  val density = LocalDensity.current
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SelectionContent(
        title = "Type of damage",
        optionsList = listOf("Front".repeat(12), "Back", "Water", "Other"),
        getId = ::identity,
        getDisplayText = ::identity,
        getIsSelected = { selectedOptions.contains(it) },
        onSelected = { selectedOptions.add(it) },
        smallSelectionItems = smallSelectionItems,
        lazyListState = rememberLazyListState(
          initialFirstVisibleItemScrollOffset = if (isScrolled) {
            with(density) {
              16.dp.roundToPx() + 1
            }
          } else {
            0
          },
        ),
      )
    }
  }
}
