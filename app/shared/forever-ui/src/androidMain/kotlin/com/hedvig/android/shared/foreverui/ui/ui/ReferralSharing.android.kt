package com.hedvig.android.shared.foreverui.ui.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import hedvig.resources.REFERRALS_SHARE_SHEET_TITLE
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun rememberShareHandler(): (text: String) -> Unit {
  val context = LocalContext.current
  val shareSheetTitle = stringResource(Res.string.REFERRALS_SHARE_SHEET_TITLE)
  return remember(context, shareSheetTitle) {
    { text ->
      context.showShareSheet(shareSheetTitle) { intent ->
        intent.putExtra(
          Intent.EXTRA_TEXT,
          text,
        )
        intent.type = "text/plain"
      }
    }
  }
}

@Composable
internal actual fun rememberCopyToClipboard(): (text: String) -> Unit {
  val context = LocalContext.current
  return remember(context) {
    { text -> context.copyToClipboard(text) }
  }
}
