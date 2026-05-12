package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun PuppyTopAppBar(title: String, onBack: () -> Unit, modifier: Modifier) {
  // implemented as spacing on the content for iOS since the top app bar is implemented natively
  //  Spacer(modifier.windowInsetsTopHeight(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)))
  Spacer(modifier)
}
