package com.hedvig.android.feature.profile.aboutapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.NotAvailableOnDesktop

/**
 * The license list is an HTML page bundled in the Android app's assets and shown in a web view, so there is none on
 * the JVM.
 */
@Composable
internal actual fun OpenSourceLicenses(modifier: Modifier) {
  NotAvailableOnDesktop(modifier)
}
