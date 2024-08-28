package com.hedvig.android.sample.design.showcase.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigLinearProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigThreeDotsProgressIndicator

@Composable
fun ProgressBarShowcase() {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
    modifier = Modifier.fillMaxSize(),
  ) {
    HedvigLinearProgressIndicator()
    HedvigThreeDotsProgressIndicator()
    HedvigCircularProgressIndicator()
  }
}
