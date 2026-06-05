package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack

@Composable
internal actual fun PuppyTopAppBar(onBack: () -> Unit, modifier: Modifier) {
  TopAppBarWithBack(onClick = onBack, modifier = modifier)
}
