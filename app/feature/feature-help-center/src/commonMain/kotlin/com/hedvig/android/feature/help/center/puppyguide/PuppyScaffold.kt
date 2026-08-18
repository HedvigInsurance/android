package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

// Uses [PuppyTopAppBar] so that on iOS — where the top bar is rendered natively — we don't end up
// with the native back button stacked on top of a Compose-rendered one.
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PuppyScaffold(navigateUp: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
    Column(Modifier.fillMaxSize()) {
      val consumedWindowInsets = remember { MutableWindowInsets() }
      PuppyTopAppBar(
        onBack = navigateUp,
        modifier = Modifier.onSizeChanged {
          consumedWindowInsets.insets = WindowInsets(top = it.height)
        },
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .consumeWindowInsets(consumedWindowInsets)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        Spacer(
          modifier = Modifier.windowInsetsTopHeight(
            WindowInsets.safeDrawing.exclude(consumedWindowInsets).only(WindowInsetsSides.Top),
          ),
        )
        content()
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
  }
}
