@file:OptIn(ExperimentalMaterial3Api::class)

package com.hedvig.android.sample.design.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.material3.HedvigMaterial3Theme
import com.hedvig.android.sample.design.showcase.ui.MaterialComponents

class DesignShowcaseActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      HedvigMaterial3Theme {
        Surface(Modifier.fillMaxSize()) {
          MaterialComponents()
        }
      }
    }
  }
}
