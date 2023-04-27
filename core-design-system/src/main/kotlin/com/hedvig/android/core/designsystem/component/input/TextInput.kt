package com.hedvig.android.core.designsystem.component.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.TextFieldDefaults.outlinedTextFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.material3.HedvigMaterial3Theme
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun TextInput(
  value: String,
  label: String,
  placeholder: String,
  errorMessage: String?,
  enabled: Boolean,
  onValueChange: (String) -> Unit,
  onDone: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column {
    TextField(
      label = {
        Text(
          text = label,
          color = Color(0xFF727272),
        )
      },
      placeholder = {
        Text(
          text = placeholder,
          color = Color(0xFF727272),
        )
      },
      trailingIcon = {
        if (value.isNotBlank()) {
          IconButton(
            onClick = { onValueChange("") },
            modifier = Modifier
              .padding(top = 12.dp)
              .size(18.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.Close,
              contentDescription = null,
            )
          }
        }
      },
      value = value,
      modifier = if (label != null) {
        modifier
          // Merge semantics at the beginning of the modifier chain to ensure padding is
          // considered part of the text field.
          .semantics(mergeDescendants = true) {}
          .padding(top = 0.dp)
      } else {
        modifier
      }
        .defaultMinSize(
          minWidth = TextFieldDefaults.MinWidth,
          minHeight = TextFieldDefaults.MinHeight,
        ),
      shape = RoundedCornerShape(14.dp),
      colors = TextFieldDefaults.textFieldColors(
        cursorColor = Color.Black,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
      ),
      onValueChange = onValueChange,
      enabled = enabled,
    )
    if (errorMessage != null) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp),
      ) {
        Icon(
          imageVector = Icons.Outlined.Warning,
          contentDescription = null,
          modifier = Modifier.size(14.dp),
          tint = Color(0xFFFFBF00),
        )
        Spacer(modifier = Modifier.padding(start = 4.dp))
        Text(
          text = errorMessage,
          fontSize = 14.sp,
        )
      }
    }
  }
}

@HedvigPreview
@Preview
@Composable
fun TextInputPreview() {
  HedvigTheme {
    TextInput(
      value = "Frödingsvägen 10",
      label = "Address",
      placeholder = "Testvägen 2",
      enabled = true,
      errorMessage = "Ditt personnummber st'mmer inte",
      onValueChange = {},
      onDone = {},
    )
  }
}
