package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun M2Cards() {
  Column {
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Cards",
      style = MaterialTheme.typography.h5,
    )
    Spacer(Modifier.size(16.dp))
    Card(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Card", Modifier.align(Alignment.Center))
      }
    }
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Elevated cards",
      style = MaterialTheme.typography.subtitle2,
    )
    Spacer(Modifier.size(16.dp))
    Card(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
      elevation = 8.dp,
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Card 8.dp elevated", Modifier.align(Alignment.Center))
      }
    }
    Spacer(Modifier.size(4.dp))
    Card(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
      elevation = 16.dp,
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Card 16.dp elevated", Modifier.align(Alignment.Center))
      }
    }
    Spacer(Modifier.size(16.dp))
    Card(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
      elevation = 32.dp,
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Card 32.dp elevated", Modifier.align(Alignment.Center))
      }
    }
  }
}
