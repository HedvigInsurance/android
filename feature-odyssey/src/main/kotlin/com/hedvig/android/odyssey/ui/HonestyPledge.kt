package com.hedvig.android.odyssey.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.odyssey.ClaimsFlowViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
fun HonestyPledge(viewModel: ClaimsFlowViewModel) {
  val coroutineScope = rememberCoroutineScope()

  Box(Modifier.fillMaxHeight()) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text("Honesty pledge", fontSize = 40.sp)
      Spacer(modifier = Modifier.padding(top = 20.dp))
      Text("I’m aware that Hedvig is based on trust and that the entire surplus goes to charity. To honor this, I solemnly swear to recite the event as it was and only claim the compensation I’m entitled to.")
    }
    LargeContainedButton(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(16.dp),
      onClick = {
        coroutineScope.launch {
          viewModel.createClaim()
          viewModel.onNext()
        }
      },
    ) {
      Text("Continue")
    }
  }
}
