package com.feature.changeaddress.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.changeaddress.ChangeAddressUiState
import com.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack

@Composable
internal fun ChangeAddressSelectHousingTypeDestination(
  viewModel: ChangeAddressViewModel,
  navigateBack: () -> Unit,
  onSelectHousingType: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val selectedHousingType = uiState.apartmentOwnerType

  Surface(Modifier.fillMaxSize()) {
    Column {
      val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBarWithBack(
        onClick = navigateBack,
        title = "",
        scrollBehavior = topAppBarScrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      )
      Spacer(modifier = Modifier.padding(top = 48.dp))
      Text(
        text = "Välj din bostadstyp",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(modifier = Modifier.padding(bottom = 114.dp))

      Column(
        Modifier
          .fillMaxSize()
          .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
          .verticalScroll(rememberScrollState())
          .padding(16.dp)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        // Show error explaining we dont support moving flow for Villa yet
        LargeContainedButton(onClick = { }) {
          Text(text = "Villa")
        }
        Spacer(modifier = Modifier.padding(top = 6.dp))
        LargeContainedButton(
          onClick = {
            viewModel.onSelectHousingType(ApartmentOwnerType.RENT)
            onSelectHousingType()
          },
        ) {
          Text(text = "Hyresrätt")
        }
        Spacer(modifier = Modifier.padding(top = 6.dp))
        LargeContainedButton(
          onClick = {
            viewModel.onSelectHousingType(ApartmentOwnerType.OWN)
            onSelectHousingType()
          },
        ) {
          Text(text = "Bostadsrätt")
        }
        Spacer(modifier = Modifier.padding(top = 6.dp))
        AddressInfoCard()
        Spacer(modifier = Modifier.padding(top = 6.dp))
        LargeContainedButton(
          onClick = {
            viewModel.onSelectHousingType(ApartmentOwnerType.OWN)
            onSelectHousingType()
          },
        ) {
          Text(text = "Fortsätt")
        }
      }
    }
  }
}
