package com.hedvig.android.feature.login.marketing.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import hedvigandroid.feature_login.generated.resources.Res
import hedvigandroid.feature_login.generated.resources.login_still_9x16
import org.jetbrains.compose.resources.painterResource

/**
 * The still frame of the login video, shown in place of playing it.
 */
@Composable
actual fun LoginBackgroundVideo() {
  Image(
    painter = painterResource(Res.drawable.login_still_9x16),
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize(),
  )
}
