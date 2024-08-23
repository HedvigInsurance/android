package com.hedvig.android.sample.design.showcase.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.CircularProgressBar
import com.hedvig.android.design.system.hedvig.HedvigLinearProgressBar
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ThreeDotsLoading

@Composable
fun ProgressBarShowcase() {
  Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      HedvigLinearProgressBar(Modifier.padding(32.dp))
      Spacer(Modifier.height(32.dp))
      ThreeDotsLoading()
      Spacer(Modifier.height(32.dp))
      CircularProgressBar()
    }

  }

}
