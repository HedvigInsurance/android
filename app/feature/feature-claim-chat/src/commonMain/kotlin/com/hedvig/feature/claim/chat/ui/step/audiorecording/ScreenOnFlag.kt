package com.hedvig.feature.claim.chat.ui.step.audiorecording

import androidx.compose.runtime.Composable

/**
 * While this composable is in composition the phone screen stays awake. This is automatically cleared when the
 * composable leaves the composition.
 */
@Composable
internal expect fun ScreenOnFlag()
