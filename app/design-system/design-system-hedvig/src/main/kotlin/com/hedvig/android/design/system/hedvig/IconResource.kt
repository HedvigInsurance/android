package com.hedvig.android.design.system.hedvig

import androidx.compose.ui.graphics.vector.ImageVector

sealed interface IconResource {
  data class Vector(val imageVector: ImageVector) : IconResource

  data class Painter(val painterResId: Int) : IconResource
}
