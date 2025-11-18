package com.hedvig.feature.claim.chat.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.feature.claim.chat.ConversationItem
import com.hedvig.feature.claim.chat.data.StepContent

@Composable
fun Summary(summary: ConversationItem.Summary, onSubmit: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = "Summary",
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )

    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      summary.items.forEach {
        SummaryItemRow(item = it)
      }
    }

    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

    Button(
      onClick = onSubmit,
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp)
        .height(56.dp),
    ) {
      Text("Submit")
    }
  }
}

@Composable
fun SummaryItemRow(item: StepContent.Summary.Item) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = item.title,
      style = MaterialTheme.typography.bodyLarge,
    )

    Text(
      text = item.value,
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}
