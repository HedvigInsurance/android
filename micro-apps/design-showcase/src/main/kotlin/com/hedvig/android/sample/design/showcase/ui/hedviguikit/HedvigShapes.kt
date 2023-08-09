package com.hedvig.android.sample.design.showcase.ui.hedviguikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircleExtraLarge
import com.hedvig.android.core.designsystem.material3.squircleExtraSmall
import com.hedvig.android.core.designsystem.material3.squircleLarge
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.material3.squircleSmall
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun HedvigShapes() {
  Box(
    modifier = Modifier.fillMaxSize().zoomable(rememberZoomState(20f)),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      for (i in 1..4) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          for (j in 1..5) {
            Box(
              Modifier
                .background(Color(0xFF121212 + (0x323212 * i)), j.toSquircleShape())
                .size(16.dp * i),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun Int.toSquircleShape(): Shape {
  return when (this) {
    1 -> MaterialTheme.shapes.squircleExtraSmall
    2 -> MaterialTheme.shapes.squircleSmall
    3 -> MaterialTheme.shapes.squircleMedium
    4 -> MaterialTheme.shapes.squircleLarge
    5 -> MaterialTheme.shapes.squircleExtraLarge
    else -> error("")
  }
}
