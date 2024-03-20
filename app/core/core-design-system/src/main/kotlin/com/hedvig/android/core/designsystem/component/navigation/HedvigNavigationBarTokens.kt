package com.hedvig.android.core.designsystem.component.navigation

import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.tokens.HedvigColorSchemeKeyTokens
import com.hedvig.android.core.designsystem.component.tokens.HedvigShapeKeyTokens
import com.hedvig.android.core.designsystem.component.tokens.HedvigTypographyKeyTokens

internal object HedvigNavigationBarTokens {
  val ActiveIconColor = HedvigColorSchemeKeyTokens.OnSecondaryContainer
  val ActiveIndicatorColor = HedvigColorSchemeKeyTokens.SecondaryContainer
  val ActiveIndicatorHeight = 32.0.dp
  val ActiveIndicatorShape = HedvigShapeKeyTokens.CornerFull
  val ActiveIndicatorWidth = 64.0.dp
  val ActiveLabelTextColor = HedvigColorSchemeKeyTokens.OnSurface
  val ContainerHeight = 80.0.dp
  val IconSize = 24.0.dp
  val InactiveIconColor = HedvigColorSchemeKeyTokens.OnSurfaceVariant
  val InactiveLabelTextColor = HedvigColorSchemeKeyTokens.OnSurfaceVariant
  val LabelTextFont = HedvigTypographyKeyTokens.LabelMedium
}
