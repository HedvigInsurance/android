package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// The top app bar on iOS is implemented natively, so this is rendered as empty spacing.
@Composable
internal actual fun PuppyTopAppBar(onBack: () -> Unit, modifier: Modifier) {
  Spacer(modifier)
}
