package com.hedvig.android.feature.addon.purchase.ui.travelinsuranceplusexplanation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Medium
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Blue
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.PerilData
import com.hedvig.android.design.system.hedvig.PerilDefaults.PerilSize.Small
import com.hedvig.android.design.system.hedvig.PerilList
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.TravelInsurancePlusExplanation.TravelPerilData
import hedvig.resources.R

@Composable
internal fun TravelInsurancePlusExplanationDestination(travelPerilData: List<TravelPerilData>, navigateUp: () -> Unit) {
  TravelInsurancePlusExplanationScreen(
    perilData = remember(travelPerilData) {
      travelPerilData.map {
        PerilData(
          title = it.title,
          description = it.description,
          covered = it.covered,
          colorCode = it.colorCode,
        )
      }
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun TravelInsurancePlusExplanationScreen(perilData: List<PerilData>, navigateUp: () -> Unit) {
  HedvigScaffold(navigateUp) {
    FlowHeading(
      stringResource(R.string.ADDON_FLOW_TRAVEL_INFORMATION_TITLE),
      stringResource(R.string.ADDON_FLOW_TRAVEL_INFORMATION_DESCRIPTION),
      Modifier.fillMaxWidth().padding(horizontal = 18.dp),
    )
    Spacer(Modifier.height(32.dp))
    HighlightLabel(
      labelText = stringResource(R.string.ADDON_LEARN_MORE_LABEL),
      size = Medium,
      color = Blue(LIGHT),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    PerilList(perilData, Small, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelInsurancePlusExplanationScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelInsurancePlusExplanationScreen(
        perilData = List(4) { index ->
          PerilData(
            title = "Title$index",
            description = "Description$index",
            covered = listOf("Covered#$index", "Also covered#$index"),
            colorCode = "#FFD0ECFB",
          )
        },
        navigateUp = {},
      )
    }
  }
}
