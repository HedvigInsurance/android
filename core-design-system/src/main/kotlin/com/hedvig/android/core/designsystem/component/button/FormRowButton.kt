package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FormRowButton(mainText: String, secondaryText: String, onClick: () -> Unit) {
  Column {
    LargeContainedButton(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(
        backgroundColor = if (MaterialTheme.colors.isLight) {
          MaterialTheme.colors.surface
        } else {
          MaterialTheme.colors.surface
        },
        contentColor = MaterialTheme.colors.onSurface,
      ),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text(text = mainText, maxLines = 1)
        }
        Column {
          Text(text = secondaryText, maxLines = 1)
        }
      }
    }
  }
}

