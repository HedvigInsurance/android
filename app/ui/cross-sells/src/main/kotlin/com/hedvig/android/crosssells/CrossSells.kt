package com.hedvig.android.crosssells

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.material3.lightTypeContainer
import com.hedvig.android.core.designsystem.material3.onLightTypeContainer
import com.hedvig.android.core.designsystem.material3.squircleLarge
import com.hedvig.android.data.contract.android.CrossSell
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.fade
import com.hedvig.android.placeholder.placeholder
import com.hedvig.android.placeholder.shimmer
import hedvig.resources.R

@Composable
fun ColumnScope.CrossSellsSection(
  showNotificationBadge: Boolean,
  crossSells: List<CrossSell>,
  onCrossSellClick: (String) -> Unit,
) {
  CrossSellsSubHeaderWithDivider(showNotificationBadge)
  for ((index, crossSell) in crossSells.withIndex()) {
    CrossSellItem(crossSell, onCrossSellClick, Modifier.padding(horizontal = 16.dp))
    if (index != crossSells.lastIndex) {
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
fun ColumnScope.CrossSellItemPlaceholder() {
  CrossSellsSubHeaderWithDivider(false)
  CrossSellItem(
    crossSellTitle = "HHHH",
    crossSellSubtitle = "HHHHHHHH\nHHHHHHHHHHH",
    storeUrl = "",
    type = CrossSell.CrossSellType.HOME,
    onCrossSellClick = {},
    isLoading = true,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
}

@Composable
private fun ColumnScope.CrossSellsSubHeaderWithDivider(showNotificationBadge: Boolean) {
  Spacer(Modifier.height(32.dp))
  NotificationSubheading(
    text = stringResource(R.string.insurance_tab_cross_sells_title),
    showNotification = showNotificationBadge,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
  HorizontalDivider(Modifier.padding(horizontal = 16.dp))
  Spacer(Modifier.height(16.dp))
}

@Composable
private fun CrossSellItem(crossSell: CrossSell, onCrossSellClick: (String) -> Unit, modifier: Modifier = Modifier) {
  CrossSellItem(
    crossSellTitle = crossSell.title,
    crossSellSubtitle = crossSell.subtitle,
    storeUrl = crossSell.storeUrl,
    type = crossSell.type,
    onCrossSellClick = onCrossSellClick,
    modifier = modifier,
    isLoading = false,
  )
}

@Composable
private fun CrossSellItem(
  crossSellTitle: String,
  crossSellSubtitle: String,
  storeUrl: String,
  type: CrossSell.CrossSellType,
  onCrossSellClick: (String) -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.heightIn(64.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      painter = painterResource(id = type.iconRes()),
      contentDescription = null,
      modifier = Modifier
        .size(48.dp)
        .placeholder(
          visible = isLoading,
          highlight = PlaceholderHighlight.fade(),
          shape = MaterialTheme.shapes.squircleLarge,
        ),
    )
    Spacer(Modifier.width(16.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = crossSellTitle,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.shimmer()),
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = crossSellSubtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.shimmer()),
      )
    }
    Spacer(Modifier.width(16.dp))
    HedvigContainedSmallButton(
      text = stringResource(R.string.cross_sell_get_price),
      onClick = {
        onCrossSellClick(storeUrl)
      },
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.lightTypeContainer,
        contentColor = MaterialTheme.colorScheme.onLightTypeContainer,
      ),
      modifier = Modifier.placeholder(
        visible = isLoading,
        highlight = PlaceholderHighlight.shimmer(),
        shape = MaterialTheme.shapes.squircleLarge,
      ),
      enabled = !isLoading,
    )
  }
}

@Composable
private fun NotificationSubheading(text: String, showNotification: Boolean, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // We want the notification to stick until we leave the screen, even after we've "cleared" it.
    var stickyShowNotification by remember { mutableStateOf(showNotification) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
      val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
          stickyShowNotification = false
        }
      }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
      }
    }
    AnimatedVisibility(stickyShowNotification) {
      Row {
        Canvas(Modifier.size(8.dp)) {
          drawCircle(Color.Red)
        }
        Spacer(Modifier.width(8.dp))
      }
    }
    Text(text = text)
  }
}

private fun CrossSell.CrossSellType.iconRes(): Int = when (this) {
  CrossSell.CrossSellType.PET -> R.drawable.ic_pillow_pet
  CrossSell.CrossSellType.HOME -> R.drawable.ic_pillow_home
  CrossSell.CrossSellType.ACCIDENT -> R.drawable.ic_pillow_accident
  CrossSell.CrossSellType.CAR -> R.drawable.ic_pillow_car
  CrossSell.CrossSellType.UNKNOWN -> R.drawable.ic_pillow_home
}
