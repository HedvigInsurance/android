package com.hedvig.android.odyssey.input.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import hedvig.resources.R

@Composable
fun PhoneNumber(
  currentPhoneNumber: String,
  onPhoneNumber: (String) -> Unit,
  updatePhoneNumber: () -> Unit,
  onNext: () -> Unit,
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
          text = stringResource(R.string.message_claims_ask_phone),
          modifier = Modifier.padding(12.dp),
          fontSize = 16.sp,
        )
      }
    }

    Column(
      modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      TextField(
        value = currentPhoneNumber,
        onValueChange = onPhoneNumber,
        placeholder = {
          Text("070000000")
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
      )

      Text(
        stringResource(R.string.ODYSSEY_PHONE_NUMBER_LABEL),
        modifier = Modifier.padding(
          start = 16.dp,
          top = 4.dp,
          bottom = 8.dp,
        ),
      )

      Spacer(modifier = Modifier.padding(vertical = 12.dp))

      LargeContainedTextButton(
        onClick = {
          updatePhoneNumber()
          onNext()
        },
        text = stringResource(R.string.general_continue_button),
      )
    }
  }
}
