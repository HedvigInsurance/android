package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack

@Composable
internal actual fun PuppyTopAppBar(title: String, onBack: () -> Unit, modifier: Modifier) {
  TopAppBarWithBack(title = title, onClick = onBack, modifier = modifier)
}
