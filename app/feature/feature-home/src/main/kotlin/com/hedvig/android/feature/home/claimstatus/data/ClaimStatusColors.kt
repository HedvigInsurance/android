package com.hedvig.android.feature.home.claimstatus.data

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hedvig.android.core.designsystem.theme.forever_orange_300
import com.hedvig.android.core.designsystem.theme.forever_orange_500
import com.hedvig.android.core.designsystem.theme.lavender_200
import com.hedvig.android.core.designsystem.theme.lavender_400

internal object ClaimStatusColors {
  object Pill {
    val paid: Color
      @Composable
      get() = if (isSystemInDarkTheme()) {
        lavender_400
      } else {
        lavender_200
      }

    val reopened: Color
      @Composable
      get() = if (isSystemInDarkTheme()) {
        forever_orange_500
      } else {
        forever_orange_300
      }
  }

  object Progress {
    val paid: Color
      @Composable
      get() = MaterialTheme.colors.secondary

    val reopened: Color
      @Composable
      get() = forever_orange_500
  }
}
