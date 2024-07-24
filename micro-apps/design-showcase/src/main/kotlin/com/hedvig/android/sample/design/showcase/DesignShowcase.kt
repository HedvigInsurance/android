package com.hedvig.android.sample.design.showcase

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.sample.design.showcase.button.ShowcaseButton
import com.hedvig.android.sample.design.showcase.icons.ShowcaseIcons
import com.hedvig.android.sample.design.showcase.radio.ShowCaseRadioGroups
import com.hedvig.android.sample.design.showcase.textfield.ShowcaseTextField

@Composable
internal fun DesignShowcase(modifier: Modifier = Modifier) {
  Box(modifier) {
    if (showIcons) {
      ShowcaseIcons()
    } else if (showButton) {
      ShowcaseButton()
    } else if (showTextField) {
      ShowcaseTextField()
    } else {
      ShowCaseRadioGroups()
    }
  }
}

private val showIcons = false
private val showButton = false
private val showTextField = false
