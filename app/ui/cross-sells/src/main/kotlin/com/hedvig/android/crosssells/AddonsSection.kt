package com.hedvig.android.crosssells

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import hedvig.resources.INSURANCE_ADDONS_SUBHEADING
import hedvig.resources.Res.string
import hedvig.resources.cross_sell_see_price
import org.jetbrains.compose.resources.stringResource

/**
 * The "Addons" heading and one [PillowRow] per add-on. Shown on Home, the Insurances tab and the
 * cross-sell sheet, which is why it lives here rather than in one of those features.
 *
 * [onAddonClick] receives the add-on's eligible insurance ids and is expected to open the add-on
 * purchase flow; unlike a cross-sell row there is no store URL to fall back on.
 */
@Composable
fun AddonsSection(
  addons: List<AddonBannerInfo>,
  onAddonClick: (eligibleInsuranceIds: List<String>) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  headingStyle: TextStyle = HedvigTheme.typography.bodySmall,
) {
  Column(modifier) {
    HedvigText(
      text = stringResource(string.INSURANCE_ADDONS_SUBHEADING),
      style = headingStyle,
      modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(16.dp))
    for ((index, addon) in addons.withIndex()) {
      PillowRow(
        title = addon.title,
        subtitle = addon.description,
        pillowImage = null,
        pillow = { AddonPillow(addon.flowType) },
        buttonText = stringResource(string.cross_sell_see_price),
        onButtonClick = { onAddonClick(addon.eligibleInsurancesIds) },
        imageLoader = imageLoader,
        modifier = Modifier.fillMaxWidth(),
        buttonSize = ButtonSize.Small,
      )
      if (index != addons.lastIndex) {
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}
