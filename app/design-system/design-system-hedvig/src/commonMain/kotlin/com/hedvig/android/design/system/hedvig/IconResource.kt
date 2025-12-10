package com.hedvig.android.design.system.hedvig

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.DrawableResource

sealed interface IconResource {
  data class Vector(val imageVector: ImageVector) : IconResource

  data class Painter(val painterResource: DrawableResource) : IconResource
}
