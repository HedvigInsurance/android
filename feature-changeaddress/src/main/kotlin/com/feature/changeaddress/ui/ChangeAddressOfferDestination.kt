package com.feature.changeaddress.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.feature.changeaddress.data.MoveQuote

@Composable
fun ChangeAddressOfferDestination(quotes: List<MoveQuote>) {
  Box(Modifier.fillMaxSize()) {
    Column {
      Text(text = "Quote street:")
      Text(text = quotes.firstOrNull()?.address?.street ?: "No quote found")
    }
  }
}
