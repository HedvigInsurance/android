package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * Hands the member over to the Swish app to approve a payin setup, following
 * https://developer.swish.nu/documentation/guides/trigger-the-swish-app.
 *
 * [isSwishInstalled] decides whether the approval screen offers to open Swish at all. [open] is safe
 * to call either way: with no Swish app on the device it falls back to viewing the url.
 */
@Stable
internal interface SwishAppHandover {
  val isSwishInstalled: Boolean

  fun open(approvalUrl: String)
}

/**
 * @param allowSandboxApp whether the Swish sandbox app counts as installed. Only it can approve the
 *   orders the staging backend issues, so builds pointing there accept it in place of the real app.
 */
@Composable
internal expect fun rememberSwishAppHandover(allowSandboxApp: Boolean): SwishAppHandover
