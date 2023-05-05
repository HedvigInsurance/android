package com.feature.changeaddress.ui

import HousingType
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.changeaddress.ChangeAddressUiState
import com.feature.changeaddress.ChangeAddressViewModel
import com.feature.changeaddress.ValidatedInput
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.ui.R
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import toDisplayName

@Composable
internal fun ChangeAddressSelectHousingTypeDestination(
  viewModel: ChangeAddressViewModel,
  navigateBack: () -> Unit,
  onHousingTypeSelected: () -> Unit,
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
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        HousingType.VILLA.radiobuttonRow(viewModel, selectedHousingType)
        Spacer(modifier = Modifier.padding(top = 12.dp))
        HousingType.APARTMENT_OWN.radiobuttonRow(viewModel, selectedHousingType)
        Spacer(modifier = Modifier.padding(top = 12.dp))
        HousingType.APARTMENT_RENT.radiobuttonRow(viewModel, selectedHousingType)
        Spacer(modifier = Modifier.padding(top = 8.dp))
        AddressInfoCard()
        Spacer(modifier = Modifier.padding(top = 8.dp))
        LargeContainedButton(
          onClick = {
            onHousingTypeSelected()
          },
          modifier = Modifier.padding(16.dp),
        ) {
          Text(text = "Fortsätt")
        }
      }
    }
  }
}

@Composable
private fun HousingType.radiobuttonRow(
  viewModel: ChangeAddressViewModel,
  selectedHousingType: ValidatedInput<HousingType?>,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.onPrimary)
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .clickable {
        viewModel.onSelectHousingType(this)
      },
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      painter = painterResource(id = R.drawable.ic_pillow),
      contentDescription = "",
      modifier = Modifier.size(48.dp),
    )
    Spacer(modifier = Modifier.padding(12.dp))
    Text(
      text = this@radiobuttonRow.toDisplayName(),
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxHeight(),
    )
    Spacer(modifier = Modifier.weight(1f))
    RadioButton(
      selected = selectedHousingType.input == this@radiobuttonRow,
      onClick = {
        viewModel.onSelectHousingType(this@radiobuttonRow)
      },
      modifier = Modifier.padding(end = 12.dp),
    )
  }
}
