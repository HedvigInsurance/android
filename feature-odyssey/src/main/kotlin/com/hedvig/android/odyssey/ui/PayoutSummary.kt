package com.hedvig.android.odyssey.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.ClaimsFlowViewModel
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.common.remote.money.format
import kotlinx.coroutines.launch

@Composable
fun PayoutSummary(viewModel: ClaimsFlowViewModel) {

  val viewState by viewModel.viewState.collectAsState()
  val resolution = viewState.claim?.resolutions?.filterIsInstance<Resolution.SingleItemPayout>()?.firstOrNull()
    ?: throw java.lang.IllegalArgumentException()
  val coroutineScope = rememberCoroutineScope()

  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    Column {

      Spacer(modifier = Modifier.padding(top = 28.dp))

      Text("Your compensation", style = MaterialTheme.typography.h5)

      Spacer(modifier = Modifier.padding(top = 33.dp))

      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Purchase price")
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          Text(text = resolution.purchasePrice.format())
        }
      }

      Divider(modifier = Modifier.padding(vertical = 16.dp))

      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Deductible")
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          Text(text = "-${resolution.deductible.format()}")
        }
      }

      Divider(modifier = Modifier.padding(vertical = 16.dp))

      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(text = "You will receive")
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          Text(text = resolution.payoutAmount.format())
        }
      }

      Spacer(modifier = Modifier.padding(top = 29.dp))

      Text(
        text = "If the repair cost exceeds the estimation, you can upload the invoice afterwards and we'll compensate you the difference.",
        style = MaterialTheme.typography.caption,
      )

      Spacer(modifier = Modifier.padding(top = 48.dp))

      Text("Payout method", style = MaterialTheme.typography.h5)

      Text("Connected bank account")
    }

    LargeContainedTextButton(
      onClick = {
        coroutineScope.launch {
          viewModel.openClaimAndPayout(resolution.payoutAmount)
        }
      },
      text = "Payout ${resolution.payoutAmount.format()}",
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}
