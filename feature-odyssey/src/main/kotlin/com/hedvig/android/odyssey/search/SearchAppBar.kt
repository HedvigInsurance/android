package com.hedvig.android.odyssey.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.ui.appbar.CenterAlignedTopAppBar
import com.hedvig.android.odyssey.R

@Composable
fun SearchAppBar(
  viewState: SearchViewState,
  cancelAutoCompletion: () -> Unit,
  setNewTextInput: (String) -> Unit,
  focusRequester: FocusRequester,
  onFocus: (Boolean) -> Unit,
  closeKeyboard: () -> Unit,
  contentPadding: PaddingValues,
) {
  Surface(color = MaterialTheme.colors.background) {
    Column(Modifier.padding(contentPadding)) {
      CenterAlignedTopAppBar(
        title = "",
        onClick = { cancelAutoCompletion() },
        backgroundColor = MaterialTheme.colors.background,
        icon = Icons.Filled.ArrowBack,
      )
      AddressInput(
        viewState = viewState,
        setNewTextInput = setNewTextInput,
        focusRequester = focusRequester,
        closeKeyboard = closeKeyboard,
        onFocus = onFocus
      )
    }
  }
}

@Composable
private fun AddressInput(
  viewState: SearchViewState,
  setNewTextInput: (String) -> Unit,
  focusRequester: FocusRequester,
  closeKeyboard: () -> Unit,
  onFocus: (Boolean) -> Unit,
) {
  Row(modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
    Image(
      painter = painterResource(R.drawable.ic_search),
      contentDescription = "Search",
      modifier = Modifier
        .align(Alignment.CenterVertically)
        .size(24.dp),
    )

    Column(
      horizontalAlignment = Alignment.Start,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      var textFieldValue by remember {
        val text = viewState.input ?: ""
        mutableStateOf(
          TextFieldValue(
            text = text,
            selection = TextRange(text.length),
          ),
        )
      }
      LaunchedEffect(viewState.input) {
        textFieldValue = textFieldValue.copy(
          text = viewState.input ?: "",
          selection = TextRange(viewState.input?.length ?: 0),
        )
      }

      BasicTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
          // TextFieldValue changes on more occasions than just text change. Do this to emulate the
          //  functionality of the onValueChange of the BasicTextField that takes a normal String
          if (textFieldValue.text != newTextFieldValue.text) {
            setNewTextInput(newTextFieldValue.text)
          }
          textFieldValue = newTextFieldValue
        },
        textStyle = LocalTextStyle.current.copy(
          textAlign = TextAlign.Start,
          color = LocalContentColor.current.copy(LocalContentAlpha.current),
          fontSize = 28.sp,
        ),
        keyboardActions = KeyboardActions(
          onDone = { closeKeyboard() },
        ),
        singleLine = true,
        cursorBrush = SolidColor(LocalContentColor.current),
        modifier = Modifier
          .focusRequester(focusRequester)
          .onFocusChanged {
            onFocus(it.hasFocus)
          }
          .fillMaxWidth(),
      )
    }
  }
}
