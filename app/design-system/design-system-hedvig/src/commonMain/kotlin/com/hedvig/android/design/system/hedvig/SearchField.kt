package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Search
import hedvig.resources.GENERAL_REMOVE
import hedvig.resources.Res
import hedvig.resources.SEARCH_PLACEHOLDER
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchField(
  searchQuery: String?,
  focusRequester: FocusRequester,
  onClearSearch: () -> Unit,
  onKeyboardAction: () -> Unit,
  onSearchChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(40.dp)
      .background(
        color = HedvigTheme.colorScheme.surfacePrimary,
        shape = HedvigTheme.shapes.cornerMedium,
      ),
  ) {
    BasicTextField(
      value = searchQuery ?: "",
      onValueChange = onSearchChange,
      cursorBrush = SolidColor(HedvigTheme.colorScheme.fillPrimary),
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(focusRequester),
      textStyle = HedvigTheme.typography.bodySmall.copy(color = HedvigTheme.colorScheme.textPrimary),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(
        onSearch = {
          onKeyboardAction()
        },
      ),
      decorationBox = { innerTextField ->
        Row(
          Modifier
            .fillMaxSize(),
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            HedvigIcons.Search,
            contentDescription = null,
            modifier = Modifier
              .alpha(0.60f)
              .padding(8.dp)
              .size(24.dp),
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart,
          ) {
            if (searchQuery.isNullOrEmpty()) {
              HedvigText(
                text = stringResource(Res.string.SEARCH_PLACEHOLDER),
                style = HedvigTheme.typography.bodySmall.copy(color = LocalContentColor.current),
                modifier = Modifier
                  .alpha(0.60f),
              )
            }
            innerTextField()
          }
          ClearSearchIcon(
            onClick = onClearSearch,
            tint = HedvigTheme.colorScheme.fillPrimary,
            modifier = Modifier
              .padding(8.dp)
              .size(24.dp)
              .then(
                if (searchQuery.isNullOrEmpty()) {
                  Modifier.withoutPlacement()
                } else {
                  Modifier
                },
              ),
          )
        }
      },
    )
  }
}

@Composable
private fun ClearSearchIcon(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
) {
  IconButton(
    onClick = onClick,
    modifier = modifier,
  ) {
    Icon(
      HedvigIcons.Close,
      contentDescription = stringResource(Res.string.GENERAL_REMOVE),
      tint = tint,
    )
  }
}
