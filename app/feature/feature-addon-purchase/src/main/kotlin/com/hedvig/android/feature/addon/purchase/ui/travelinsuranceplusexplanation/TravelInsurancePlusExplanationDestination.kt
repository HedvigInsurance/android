package com.hedvig.android.feature.addon.purchase.ui.travelinsuranceplusexplanation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
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
import com.hedvig.android.feature.addon.purchase.navigation.PerilComparisonParams
import hedvig.resources.ADDON_FLOW_TRAVEL_INFORMATION_DESCRIPTION
import hedvig.resources.ADDON_FLOW_TRAVEL_INFORMATION_TITLE
import hedvig.resources.ADDON_LEARN_MORE_LABEL
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelInsurancePlusExplanationDestination(
  params: PerilComparisonParams,
  navigateUp: () -> Unit,
) {
  TravelInsurancePlusExplanationScreen(
    travelPerilData = params,
    navigateUp = navigateUp,
  )
}

@Composable
private fun TravelInsurancePlusExplanationScreen(
  travelPerilData: PerilComparisonParams,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(navigateUp) {
    FlowHeading(
      travelPerilData.whatsIncludedPageTitle,
      travelPerilData.whatsIncludedPageDescription,
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp),
    ) //todo: change when BE allows
    Spacer(Modifier.height(32.dp))
    HighlightLabel(
      labelText = stringResource(Res.string.ADDON_LEARN_MORE_LABEL),
      size = Medium,
      color = Blue(LIGHT),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    travelPerilData.perilList.forEachIndexed { index, item ->
      val perilData = remember(travelPerilData) {
        item.second.map {
          PerilData(
            title = it.title,
            description = it.description,
            covered = it.covered,
            colorCode = it.colorCode,
          )
        }
      }
      item.first?.let {
        Spacer(Modifier.height(8.dp))
        HedvigText(it,
        modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
      }
      PerilList(
        perilData, Small,
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      if (index!=travelPerilData.perilList.lastIndex) {
        Spacer(Modifier.height(16.dp))
      }
    }
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelInsurancePlusExplanationScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelInsurancePlusExplanationScreen(
        PerilComparisonParams(
          whatsIncludedPageTitle = "What is this addon about",
          whatsIncludedPageDescription = "This addon is about that",
          perilList =  List(4) { index ->
            "Addon 1" to List(4) {
              TravelPerilData(
                title = "Title$index",
                description = "Description$index",
                covered = listOf("Covered#$index", "Also covered#$index"),
                colorCode = "#FFD0ECFB",
              )
            }
          }
        ),
        navigateUp = {},
      )
    }
  }
}
