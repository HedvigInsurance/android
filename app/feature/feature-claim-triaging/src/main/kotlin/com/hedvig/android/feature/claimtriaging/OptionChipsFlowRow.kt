package com.hedvig.android.feature.claimtriaging

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import arrow.core.identity
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.HedvigChip
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> OptionChipsFlowRow(
  items: ImmutableList<T>,
  itemDisplayName: (T) -> String,
  selectedItem: T?,
  onItemClick: (T) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    for (item in items) {
      key(item) {
        val isPreview = LocalInspectionMode.current
        val showChipAnimatable = remember {
          Animatable(if (isPreview) 1.0f else 0.0f)
        }
        LaunchedEffect(Unit) {
          delay(Random.nextDouble(0.3, 0.6).seconds)
          showChipAnimatable.animateTo(
            1.0f,
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioLowBouncy,
              stiffness = Spring.StiffnessLow,
            ),
          )
        }
        HedvigChip(
          item = item,
          showChipAnimatable = showChipAnimatable,
          itemDisplayName = itemDisplayName,
          isSelected = item == selectedItem,
          onItemClick = onItemClick,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOptionChipsFlowRow() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Box(modifier = Modifier.padding(16.dp)) {
        val items = remember {
          List(12) {
            val displayName = buildString { repeat((4..14).random()) { append(('a'..'z').random()) } }
            displayName
          }.toImmutableList()
        }
        OptionChipsFlowRow(
          items,
          ::identity,
          items[3],
          {},
        )
      }
    }
  }
}
