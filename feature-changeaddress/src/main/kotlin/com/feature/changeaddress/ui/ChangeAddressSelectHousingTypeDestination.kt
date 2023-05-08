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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.changeaddress.ChangeAddressUiState
import com.feature.changeaddress.ChangeAddressViewModel
import com.feature.changeaddress.ValidatedInput
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
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
        text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_SELECT_HOUSING_TYPE_TITLE),
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
        HousingType.VILLA.RadiobuttonRow(viewModel, selectedHousingType)
        Spacer(modifier = Modifier.padding(top = 8.dp))
        HousingType.APARTMENT_OWN.RadiobuttonRow(viewModel, selectedHousingType)
        Spacer(modifier = Modifier.padding(top = 8.dp))
        HousingType.APARTMENT_RENT.RadiobuttonRow(viewModel, selectedHousingType)
        Spacer(modifier = Modifier.padding(top = 8.dp))
        AddressInfoCard(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.padding(top = 8.dp))
        LargeContainedButton(
          onClick = {
            onHousingTypeSelected()
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        ) {
          Text(text = stringResource(id = hedvig.resources.R.string.general_continue_button))
        }
      }
    }
  }
}

@Composable
private fun HousingType.RadiobuttonRow(
  viewModel: ChangeAddressViewModel,
  selectedHousingType: ValidatedInput<HousingType?>,
) {
  Row(
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .fillMaxWidth()
      .background(
        color = Color(0xFFF0F0F0),
        shape = SquircleShape,
      )
      .clickable {
        viewModel.onSelectHousingType(this)
      }
      .padding(vertical = 20.dp, horizontal = 16.dp),
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
      text = stringResource(this@RadiobuttonRow.toDisplayName()),
      textAlign = TextAlign.Center,
      fontSize = 18.sp,
      modifier = Modifier.fillMaxHeight(),
    )
    Spacer(modifier = Modifier.weight(1f))
    RadioButton(
      selected = selectedHousingType.input == this@RadiobuttonRow,
      onClick = {
        viewModel.onSelectHousingType(this@RadiobuttonRow)
      },
    )
  }
}
