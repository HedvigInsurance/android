package com.hedvig.android.feature.claim.chat.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.BottomSheetStyle
import com.hedvig.android.design.system.hedvig.HedvigTheme

/**
 * The presentation shared by the claim chat's two input surfaces, text and voice.
 *
 * The design keeps the question readable while an input is open, so the scrim is lighter than the default and the
 * sheet is inset from the screen edges to read as a card floating over the conversation rather than a panel
 * replacing it.
 */
internal object ClaimChatInputSheet {
  val padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)

  val style: BottomSheetStyle
    @Composable
    get() = BottomSheetStyle(
      transparentBackground = false,
      automaticallyScrollableContent = false,
      scrimColor = HedvigTheme.colorScheme.scrim.copy(alpha = 0.2f),
    )
}
