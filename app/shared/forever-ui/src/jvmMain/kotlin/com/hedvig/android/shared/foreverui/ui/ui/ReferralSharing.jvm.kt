package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * There is no system share sheet on the JVM, so sharing puts the text on the system clipboard.
 */
@Composable
internal actual fun rememberShareHandler(): (text: String) -> Unit = ::copyToSystemClipboard

@Composable
internal actual fun rememberCopyToClipboard(): (text: String) -> Unit = ::copyToSystemClipboard

private fun copyToSystemClipboard(text: String) {
  Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
