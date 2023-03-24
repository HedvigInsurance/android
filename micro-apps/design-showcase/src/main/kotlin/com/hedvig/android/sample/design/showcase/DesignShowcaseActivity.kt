package com.hedvig.android.sample.design.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.sample.design.showcase.ui.MaterialComponents

class DesignShowcaseActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      HedvigTheme {
        MaterialComponents(calculateWindowSizeClass(this))
      }
    }
  }
}
