package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable

internal actual val WindowInsets.Companion.chatImeAnimationTarget: WindowInsets
  @Composable get() = WindowInsets.ime
