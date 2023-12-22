package com.hedvig.android.feature.help.center.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.material3.squircleMedium
import hedvig.resources.R

@Composable
fun StillNeedHelpSection(openChat: () -> Unit) {
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .background(
          shape = MaterialTheme.shapes.squircleMedium,
          color = MaterialTheme.colorScheme.surface,
        )
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      Spacer(modifier = Modifier.height(32.dp))
      Text(text = stringResource(id = R.string.HC_CHAT_QUESTION))
      Text(
        text = stringResource(id = R.string.HC_CHAT_ANSWER),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigContainedSmallButton(text = stringResource(id = R.string.HC_CHAT_BUTTON), onClick = openChat)
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
