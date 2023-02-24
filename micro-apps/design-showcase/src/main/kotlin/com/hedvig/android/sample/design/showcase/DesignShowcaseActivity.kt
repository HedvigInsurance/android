package com.hedvig.android.sample.design.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hedvig.android.core.designsystem.material3.HedvigMaterial3Theme
import com.hedvig.android.sample.design.showcase.ui.MaterialComponents

class DesignShowcaseActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      HedvigMaterial3Theme {
        MaterialComponents()
      }
    }
  }
}
