package com.hedvig.android.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
  name = "lightMode portrait",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
  name = "nightMode portrait",
  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class HedvigPreview

@Preview(
  name = "lightMode landscape",
  locale = "en",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:parent=pixel_5,orientation=landscape",
)
@Preview(
  name = "darkMode landscape",
  locale = "en",
  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:parent=pixel_5,orientation=landscape",
)
private annotation class HedvigLandscapePreview

@Preview(
  name = "lightMode tablet portrait",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
)
@Preview(
  name = "darkMode tablet portrait",
  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
)
private annotation class HedvigTabletPreview

@Preview(
  name = "lightMode tablet landscape",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Preview(
  name = "darkMode tablet landscape",
  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1280dp,height=800dp,dpi=240",
)
private annotation class HedvigTabletLandscapePreview

@HedvigPreview
@HedvigLandscapePreview
@HedvigTabletPreview
@HedvigTabletLandscapePreview
annotation class HedvigMultiScreenPreview
