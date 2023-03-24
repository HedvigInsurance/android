package com.hedvig.android.feature.businessmodel

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.businessmodel.ui.BusinessModelScreen
import org.koin.androidx.viewmodel.ext.android.getViewModel

class BusinessModelActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)
    getViewModel<BusinessModelViewModel>()
    setContent {
      HedvigTheme {
        BusinessModelScreen(
          navigateBack = { onBackPressedDispatcher.onBackPressed() },
          windowSizeClass = calculateWindowSizeClass(this),
        )
      }
    }
  }
}
