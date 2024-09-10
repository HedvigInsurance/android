package com.hedvig.android.sample.design.showcase.bigcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.Scaffold

@Composable
fun BigCardShowcase() {
  Scaffold(
    navigateUp = {},
    topAppBarText = "Screen",
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HedvigBigCard(
          onClick = {},
          labelText = "Label",
          inputText = "Input text long",
          modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
