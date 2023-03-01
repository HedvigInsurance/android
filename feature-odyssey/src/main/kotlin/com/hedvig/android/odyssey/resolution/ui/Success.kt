package com.hedvig.android.odyssey.resolution.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton

@Composable
fun Success(
  onExit: () -> Unit,
) {
  BackHandler {
    onExit()
  }

  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    Column {
      Text("Claim submitted", fontSize = 40.sp)
      Spacer(modifier = Modifier.padding(top = 8.dp))
      Text("Your claim has been submitted successfully! Please contact us in the chat if you have any questions.")
    }

    LargeContainedTextButton(
      onClick = onExit,
      text = "Continue",
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}
