package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun PuppyTopAppBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier)
