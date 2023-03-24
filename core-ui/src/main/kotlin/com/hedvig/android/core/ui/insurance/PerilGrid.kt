package com.hedvig.android.core.ui.insurance

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.PerilCard
import com.hedvig.android.core.ui.grid.HedvigGrid
import com.hedvig.android.core.ui.grid.InsideGridSpace
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader

@Composable
fun PerilGrid(
  perils: List<PerilGridData>,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  HedvigGrid(
    modifier = modifier,
    contentPadding = contentPadding,
    insideGridSpace = InsideGridSpace(8.dp),
  ) {
    for (peril in perils) {
      PerilCard(
        text = peril.text,
        iconUrl = peril.iconUrl,
        onClick = peril.onClick,
        imageLoader = imageLoader,
      )
    }
  }
}

@Immutable
class PerilGridData(
  val text: String,
  val iconUrl: String,
  val onClick: () -> Unit,
)

@HedvigPreview
@Composable
private fun PreviewPerilGrid() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PerilGrid(
        List(9) { PerilGridData("Peril#$it", "", {}) },
        rememberPreviewImageLoader(),
        contentPadding = PaddingValues(16.dp),
      )
    }
  }
}
