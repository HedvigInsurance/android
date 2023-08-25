package com.hedvig.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.hedvig.android.core.designsystem.material3.HedvigMaterial3Theme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HedvigTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  Box(
    modifier = Modifier.semantics {
      testTagsAsResourceId = true
    },
  ) {
    HedvigMaterial3Theme(
      darkTheme = darkTheme,
      content = content,
    )
  }
}
