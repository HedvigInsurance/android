package com.hedvig.android.sample.design.showcase

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.sample.design.showcase.icons.ShowcaseIcons

@Composable
fun DesignShowcase(modifier: Modifier = Modifier) {
  Box(modifier) {
    ShowcaseIcons()
  }
}
