package com.hedvig.android.odyssey.step.manualhandling

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import hedvig.resources.R

@Composable
internal fun ManualHandlingDestination(
  navigateUp: () -> Unit,
) {
  ManualHandlingScreen(navigateUp = navigateUp)
}

@Composable
private fun ManualHandlingScreen(
  navigateUp: () -> Unit,
) {
  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    Column {
      Surface(
        elevation = 2.dp,
        modifier = Modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(20),
      ) {
        Text(
          text = stringResource(R.string.message_claims_record_ok),
          modifier = Modifier.padding(12.dp),
          fontSize = 16.sp,
        )
      }
    }

    LargeContainedTextButton(
      onClick = navigateUp,
      text = stringResource(R.string.general_close_button),
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}
