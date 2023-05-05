package com.feature.changeaddress.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.changeaddress.ChangeAddressUiState
import com.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import toDisplayName

@Composable
internal fun ChangeAddressOfferDestination(
  viewModel: ChangeAddressViewModel,
  navigateBack: () -> Unit,
  onChangeAddressResult: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val quote = uiState.quotes.firstOrNull() ?: throw IllegalArgumentException("No quote found!")
  val moveIntentId = uiState.moveIntentId ?: throw IllegalArgumentException("No moveIntentId found!")

  val moveResult = uiState.successfulMoveResult

  LaunchedEffect(moveResult) {
    if (moveResult != null) {
      onChangeAddressResult()
    }
  }

  Column(Modifier.fillMaxSize()) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    TopAppBarWithBack(
      onClick = navigateBack,
      title = "Ny address",
      scrollBehavior = topAppBarScrollBehavior,
    )
    Spacer(Modifier.padding(top = 38.dp))
    HedvigCard(
      elevation = HedvigCardElevation.Elevated(2.dp),
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      Column(Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.padding(top = 16.dp))
        Text(
          text = uiState.apartmentOwnerType?.toDisplayName() ?: "Okänd",
          style = MaterialTheme.typography.headlineSmall,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.padding(top = 32.dp))
        Text(
          text = quote.premium.toString() + " kr/mån",
          style = MaterialTheme.typography.headlineLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Text(
          text = uiState.movingDate?.toString() ?: "No moving date found",
          style = MaterialTheme.typography.headlineLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.padding(top = 6.dp))
        LargeContainedButton(
          onClick = { viewModel.onAcceptQuote(moveIntentId) },
        ) {
          Text(text = "Acceptera")
        }
      }
    }
  }
}
