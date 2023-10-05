package com.hedvig.android.feature.login.marketing.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.hedvig.android.feature.login.R

@Composable
fun LoginBackgroundImage(painter: Painter = painterResource(R.drawable.login_still_9x16)) {
  Image(
    painter = painter,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize(),
  )
}
