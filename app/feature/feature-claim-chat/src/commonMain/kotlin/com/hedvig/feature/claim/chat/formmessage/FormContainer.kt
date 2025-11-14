package com.hedvig.feature.claim.chat.formmessage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FormContainer(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.End,
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
      content = content,
    )
  }
}
