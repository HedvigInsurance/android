package com.hedvig.android.feature.changeaddress.ui.extrabuildings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.feature.changeaddress.data.stringRes

@Composable
internal fun ExtraBuildingTypeContainer(
  types: List<ExtraBuildingType>,
  selectedType: ExtraBuildingType?,
  onSelected: (ExtraBuildingType) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier) {
    val nestedScrollConnection = remember {
      object : NestedScrollConnection {
        // to intercept scrolling gesture and do not close the sheet while scrolling the list
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource) = available
      } 
    }
    Column(Modifier.nestedScroll(nestedScrollConnection).verticalScroll(rememberScrollState())) {
      Text(
        text = stringResource(hedvig.resources.R.string.CHANGE_ADDRESS_EXTRA_BUILDING_CONTAINER_TITLE),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
          .padding(vertical = 12.dp, horizontal = 16.dp)
          .align(Alignment.Start),
      )
      types.forEachIndexed { index, type ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(type) }
            .padding(horizontal = 8.dp),
          verticalAlignment = CenterVertically,
        ) {
          RadioButton(
            selected = selectedType == type,
            onClick = { onSelected(type) },
          )
          Text(stringResource(id = type.stringRes()))
        }
        if (index != types.lastIndex) {
          HorizontalDivider()
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewExtraBuildingTypeContainer() {
  HedvigTheme {
    Surface {
      ExtraBuildingTypeContainer(
        types = listOf(
          ExtraBuildingType.GARAGE,
          ExtraBuildingType.CARPORT,
          ExtraBuildingType.BARN,
        ),
        onSelected = {},
        selectedType = ExtraBuildingType.BARN,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
