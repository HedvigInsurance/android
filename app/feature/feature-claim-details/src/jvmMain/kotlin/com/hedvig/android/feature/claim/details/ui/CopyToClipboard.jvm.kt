package com.hedvig.android.feature.claim.details.ui

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * Puts the text on the system clipboard. There is no toast to show the confirmation in.
 */
@Composable
internal actual fun rememberCopyToClipboard(): (text: String, confirmation: String) -> Unit = { text, _ ->
  Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
