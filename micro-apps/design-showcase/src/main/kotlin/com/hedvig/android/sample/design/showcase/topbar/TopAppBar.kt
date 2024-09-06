package com.hedvig.android.sample.design.showcase.topbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.Scaffold

@Composable
fun TopAppBarShowcase() {
  var showScreenOne by remember {mutableStateOf(true)}
  if (showScreenOne) {
    Scaffold(
      navigateUp = {},
      topAppBarText = "Screen 1"
    ) {
      Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        HedvigButton(onClick = {showScreenOne = false}, text = "Go to screen 2", enabled = true)
      }
    }
  } else {
    Scaffold(
      navigateUp = {showScreenOne = true},
      topAppBarText = "Screen 2"
    ) {
      Column (
        modifier = Modifier.fillMaxSize(), //todo: what about strange bottom padding??
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        HedvigText(text = "Screen 2", fontSize = 40.sp)
      }
    }
  }
}
