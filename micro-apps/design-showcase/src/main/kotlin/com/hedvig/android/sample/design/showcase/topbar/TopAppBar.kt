package com.hedvig.android.sample.design.showcase.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Scaffold
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

@Composable
fun TopAppBarShowcase() {
  var showScreenTwo by remember { mutableStateOf(false) }

  Scaffold(
    navigateUp = {},
    topAppBarText = "Screen 1",
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HedvigButton(onClick = { showScreenTwo = true }, text = "Go to screen 2", enabled = true)
    }
  }
  AnimatedVisibility(
    showScreenTwo,
    enter = fadeIn() + slideInHorizontally(),
    exit = fadeOut() + slideOutHorizontally(),
  ) {
    Scaffold(
      navigateUp = { showScreenTwo = false },
      topAppBarText = "Screen 2",
      topAppBarActions = {
        IconButton(onClick = { showScreenTwo = false }, Modifier.size(24.dp)) {
          Icon(HedvigIcons.Close, "")
        }
      },
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        HedvigText(text = "Screen 2", fontSize = 40.sp)
      }
    }
  }
}
