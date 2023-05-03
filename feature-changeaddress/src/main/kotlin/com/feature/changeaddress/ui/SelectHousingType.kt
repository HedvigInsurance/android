package com.example.feature.changeaddress.ui

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.example.feature.changeaddress.ChangeAddressUiState
import com.example.feature.changeaddress.ChangeAddressViewModel
import com.example.feature.changeaddress.data.ApartmentOwnerType
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack

@Composable
internal fun SelectHousingType(
  viewModel: ChangeAddressViewModel,
  navigateBack: () -> Unit,
  onSelectHousingType: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val selectedHousingType = uiState.apartmentOwnerType

  Box(
    Modifier
      .fillMaxSize()
      .padding(16.dp),
  ) {
    Column {
      val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBarWithBack(
        onClick = navigateBack,
        title = "Ändra adress",
        scrollBehavior = topAppBarScrollBehavior,
      )
      Text(
        text = "Välj bostadstyp för din nya adress",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(modifier = Modifier.padding(bottom = 52.dp, top = 40.dp))

      Column(
        Modifier
          .fillMaxSize()
          .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        // Show error explaining we dont support moving flow for Villa yet
        LargeContainedButton(onClick = { }) {
          Text(text = "Villa")
        }
        LargeContainedButton(
          onClick = {
            viewModel.onSelectHousingType(ApartmentOwnerType.RENT)
            onSelectHousingType()
          },
        ) {
          Text(text = "Hyresrätt")
        }
        LargeContainedButton(
          onClick = {
            viewModel.onSelectHousingType(ApartmentOwnerType.OWN)
            onSelectHousingType()
          },
        ) {
          Text(text = "Bostadsrätt")
        }
      }
    }
  }
}
