package com.hedvig.android.core.fileupload

import androidx.compose.runtime.Composable

/**
 * Hands a downloaded PDF to the platform so the member can view it. [applicationId] names the Android file provider.
 */
@Composable
expect fun rememberOpenPdf(applicationId: String): (DownloadedFile) -> Unit
