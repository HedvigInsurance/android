package com.hedvig.feature.claim.chat.formmessage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.feature.claim.chat.ConversationItem
import com.hedvig.feature.claim.chat.FormViews

@Composable
fun Form(
  item: ConversationItem.Form,
  onSubmit: () -> Unit,
) {
  Column(horizontalAlignment = Alignment.End) {
    FormViews(item)
    Spacer(Modifier.height(4.dp))
    Button(
      onClick = onSubmit,
    ) {
      Text("Submit Form")
    }
  }
}
