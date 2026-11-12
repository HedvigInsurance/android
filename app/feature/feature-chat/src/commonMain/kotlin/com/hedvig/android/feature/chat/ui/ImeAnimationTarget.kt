package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable

/** The insets the on-screen keyboard has when its current show or hide animation ends. */
internal expect val WindowInsets.Companion.chatImeAnimationTarget: WindowInsets
  @Composable get
