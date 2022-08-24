package com.hedvig.android.feature.charity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import coil.ImageLoader
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.charity.ui.CharityScreen
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.getViewModel

class CharityActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val imageLoader: ImageLoader = get()
    val viewModel = getViewModel<CharityViewModel>()
    setContent {
      HedvigTheme {
        val uiState by viewModel.uiState.collectAsState()
        CharityScreen(
          uiState = uiState,
          retry = { viewModel.reload() },
          goBack = { onBackPressed() },
          imageLoader = imageLoader,
        )
      }
    }
  }
}
