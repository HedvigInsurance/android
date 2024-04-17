package com.hedvig.android.sample.design.showcase

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.icon.ArrowDown
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.ArrowRight
import com.hedvig.android.design.system.hedvig.icon.ArrowUp
import com.hedvig.android.design.system.hedvig.icon.Attach
import com.hedvig.android.design.system.hedvig.icon.AttentionFilled
import com.hedvig.android.design.system.hedvig.icon.AttentionOutline
import com.hedvig.android.design.system.hedvig.icon.Camera
import com.hedvig.android.design.system.hedvig.icon.Campaign
import com.hedvig.android.design.system.hedvig.icon.Card
import com.hedvig.android.design.system.hedvig.icon.Certified
import com.hedvig.android.design.system.hedvig.icon.Chat
import com.hedvig.android.design.system.hedvig.icon.CheckFilled
import com.hedvig.android.design.system.hedvig.icon.CheckOutline
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.ChevronDown
import com.hedvig.android.design.system.hedvig.icon.ChevronDownSmall
import com.hedvig.android.design.system.hedvig.icon.ChevronLeft
import com.hedvig.android.design.system.hedvig.icon.ChevronLeftIOS
import com.hedvig.android.design.system.hedvig.icon.ChevronLeftSmall
import com.hedvig.android.design.system.hedvig.icon.ChevronRight
import com.hedvig.android.design.system.hedvig.icon.ChevronRightSmall
import com.hedvig.android.design.system.hedvig.icon.ChevronUp
import com.hedvig.android.design.system.hedvig.icon.ChevronUpSmall
import com.hedvig.android.design.system.hedvig.icon.CircleFilled
import com.hedvig.android.design.system.hedvig.icon.CircleOutline
import com.hedvig.android.design.system.hedvig.icon.Clock
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.Copy
import com.hedvig.android.design.system.hedvig.icon.Document
import com.hedvig.android.design.system.hedvig.icon.Dots
import com.hedvig.android.design.system.hedvig.icon.Download
import com.hedvig.android.design.system.hedvig.icon.EQ
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadFilled
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.design.system.hedvig.icon.ID
import com.hedvig.android.design.system.hedvig.icon.Image
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.icon.InfoOutline
import com.hedvig.android.design.system.hedvig.icon.Link
import com.hedvig.android.design.system.hedvig.icon.Lock
import com.hedvig.android.design.system.hedvig.icon.Mic
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.design.system.hedvig.icon.Notification
import com.hedvig.android.design.system.hedvig.icon.Pause
import com.hedvig.android.design.system.hedvig.icon.PaymentFilled
import com.hedvig.android.design.system.hedvig.icon.PaymentOutline
import com.hedvig.android.design.system.hedvig.icon.Play
import com.hedvig.android.design.system.hedvig.icon.Plus
import com.hedvig.android.design.system.hedvig.icon.ProfileFilled
import com.hedvig.android.design.system.hedvig.icon.ProfileOutline
import com.hedvig.android.design.system.hedvig.icon.Refresh
import com.hedvig.android.design.system.hedvig.icon.Reload
import com.hedvig.android.design.system.hedvig.icon.Search
import com.hedvig.android.design.system.hedvig.icon.Settings
import com.hedvig.android.design.system.hedvig.icon.ShieldFilled
import com.hedvig.android.design.system.hedvig.icon.ShieldOutline
import com.hedvig.android.design.system.hedvig.icon.Star
import com.hedvig.android.design.system.hedvig.icon.Swap
import com.hedvig.android.design.system.hedvig.icon.Travel
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.icon.WarningOutline

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignShowcase(modifier: Modifier = Modifier) {
  Box(modifier) {
    FlowRow(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Image(HedvigIcons.ArrowDown, null, Modifier.size(24.dp))
      Image(HedvigIcons.ArrowLeft, null, Modifier.size(24.dp))
      Image(HedvigIcons.ArrowNorthEast, null, Modifier.size(24.dp))
      Image(HedvigIcons.ArrowRight, null, Modifier.size(24.dp))
      Image(HedvigIcons.ArrowUp, null, Modifier.size(24.dp))
      Image(HedvigIcons.Attach, null, Modifier.size(24.dp))
      Image(HedvigIcons.AttentionFilled, null, Modifier.size(24.dp))
      Image(HedvigIcons.AttentionOutline, null, Modifier.size(24.dp))
      Image(HedvigIcons.Camera, null, Modifier.size(24.dp))
      Image(HedvigIcons.Campaign, null, Modifier.size(24.dp))
      Image(HedvigIcons.Card, null, Modifier.size(24.dp))
//      Image(HedvigIcons.Cart, null, Modifier.size(24.dp))
//      Image(HedvigIcons.CartAdded, null, Modifier.size(24.dp))
      Image(HedvigIcons.Certified, null, Modifier.size(24.dp))
      Image(HedvigIcons.Chat, null, Modifier.size(24.dp))
      Image(HedvigIcons.CheckFilled, null, Modifier.size(24.dp))
      Image(HedvigIcons.Checkmark, null, Modifier.size(24.dp))
      Image(HedvigIcons.CheckOutline, null, Modifier.size(24.dp))
      Image(HedvigIcons.ChevronDown, null, Modifier.size(24.dp))
      Image(HedvigIcons.ChevronDownSmall, null, Modifier.size(24.dp))
      Image(HedvigIcons.ChevronLeft, null, Modifier.size(24.dp))
      Image(HedvigIcons.ChevronLeftIOS, null, Modifier.size(24.dp))
      Image(HedvigIcons.ChevronLeftSmall, null, Modifier.size(24.dp))
      Image(HedvigIcons.ChevronRight, null, Modifier.size(24.dp))
      Image(HedvigIcons.ChevronRightSmall, null, Modifier.size(24.dp))
      Image(HedvigIcons.ChevronUp, null, Modifier.size(24.dp))
      Image(HedvigIcons.ChevronUpSmall, null, Modifier.size(24.dp))
      Image(HedvigIcons.CircleFilled, null, Modifier.size(24.dp))
      Image(HedvigIcons.CircleOutline, null, Modifier.size(24.dp))
      Image(HedvigIcons.Clock, null, Modifier.size(24.dp))
      Image(HedvigIcons.Close, null, Modifier.size(24.dp))
      Image(HedvigIcons.Copy, null, Modifier.size(24.dp))
      Image(HedvigIcons.Document, null, Modifier.size(24.dp))
      Image(HedvigIcons.Dots, null, Modifier.size(24.dp))
      Image(HedvigIcons.Download, null, Modifier.size(24.dp))
      Image(HedvigIcons.EQ, null, Modifier.size(24.dp))
      Image(HedvigIcons.HelipadFilled, null, Modifier.size(24.dp))
      Image(HedvigIcons.HelipadOutline, null, Modifier.size(24.dp))
      Image(HedvigIcons.ID, null, Modifier.size(24.dp))
      Image(HedvigIcons.Image, null, Modifier.size(24.dp))
      Image(HedvigIcons.InfoFilled, null, Modifier.size(24.dp))
      Image(HedvigIcons.InfoOutline, null, Modifier.size(24.dp))
      Image(HedvigIcons.Link, null, Modifier.size(24.dp))
      Image(HedvigIcons.Lock, null, Modifier.size(24.dp))
      Image(HedvigIcons.Mic, null, Modifier.size(24.dp))
      Image(HedvigIcons.Minus, null, Modifier.size(24.dp))
      Image(HedvigIcons.Notification, null, Modifier.size(24.dp))
      Image(HedvigIcons.Pause, null, Modifier.size(24.dp))
      Image(HedvigIcons.PaymentFilled, null, Modifier.size(24.dp))
      Image(HedvigIcons.PaymentOutline, null, Modifier.size(24.dp))
      Image(HedvigIcons.Play, null, Modifier.size(24.dp))
      Image(HedvigIcons.Plus, null, Modifier.size(24.dp))
      Image(HedvigIcons.ProfileFilled, null, Modifier.size(24.dp))
      Image(HedvigIcons.ProfileOutline, null, Modifier.size(24.dp))
      Image(HedvigIcons.Refresh, null, Modifier.size(24.dp))
      Image(HedvigIcons.Reload, null, Modifier.size(24.dp))
      Image(HedvigIcons.Search, null, Modifier.size(24.dp))
      Image(HedvigIcons.Settings, null, Modifier.size(24.dp))
      Image(HedvigIcons.ShieldFilled, null, Modifier.size(24.dp))
      Image(HedvigIcons.ShieldOutline, null, Modifier.size(24.dp))
      Image(HedvigIcons.Star, null, Modifier.size(24.dp))
      Image(HedvigIcons.Swap, null, Modifier.size(24.dp))
      Image(HedvigIcons.Travel, null, Modifier.size(24.dp))
      Image(HedvigIcons.WarningFilled, null, Modifier.size(24.dp))
      Image(HedvigIcons.WarningOutline, null, Modifier.size(24.dp))
    }
  }
}

@Preview
@Composable
private fun PreviewDesignShowcase() {
  HedvigTheme {
    DesignShowcase()
  }
}
