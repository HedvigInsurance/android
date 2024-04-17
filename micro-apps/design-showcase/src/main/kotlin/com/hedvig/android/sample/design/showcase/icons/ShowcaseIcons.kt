package com.hedvig.android.sample.design.showcase.icons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalContentColor
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
import com.hedvig.android.design.system.hedvig.icon.Cart
import com.hedvig.android.design.system.hedvig.icon.CartAdded
import com.hedvig.android.design.system.hedvig.icon.Certified
import com.hedvig.android.design.system.hedvig.icon.Chat
import com.hedvig.android.design.system.hedvig.icon.CheckFilled
import com.hedvig.android.design.system.hedvig.icon.CheckOutline
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.ChevronDown
import com.hedvig.android.design.system.hedvig.icon.ChevronDownSmall
import com.hedvig.android.design.system.hedvig.icon.ChevronLeft
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
fun ShowcaseIcons(modifier: Modifier = Modifier) {
  CompositionLocalProvider(LocalContentColor provides Color.Magenta) {
    Box(modifier) {
      FlowRow(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(HedvigIcons.ArrowDown, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ArrowLeft, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ArrowNorthEast, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ArrowRight, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ArrowUp, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Attach, null, Modifier.size(24.dp))
        Icon(HedvigIcons.AttentionFilled, null, Modifier.size(24.dp))
        Icon(HedvigIcons.AttentionOutline, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Camera, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Campaign, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Card, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Cart, null, Modifier.size(24.dp))
        Icon(HedvigIcons.CartAdded, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Certified, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Chat, null, Modifier.size(24.dp))
        Icon(HedvigIcons.CheckFilled, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Checkmark, null, Modifier.size(24.dp))
        Icon(HedvigIcons.CheckOutline, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ChevronDown, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ChevronDownSmall, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ChevronLeft, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ChevronLeftSmall, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ChevronRight, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ChevronRightSmall, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ChevronUp, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ChevronUpSmall, null, Modifier.size(24.dp))
        Icon(HedvigIcons.CircleFilled, null, Modifier.size(24.dp))
        Icon(HedvigIcons.CircleOutline, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Clock, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Close, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Copy, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Document, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Dots, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Download, null, Modifier.size(24.dp))
        Icon(HedvigIcons.EQ, null, Modifier.size(24.dp))
        Icon(HedvigIcons.HelipadFilled, null, Modifier.size(24.dp))
        Icon(HedvigIcons.HelipadOutline, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ID, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Image, null, Modifier.size(24.dp))
        Icon(HedvigIcons.InfoFilled, null, Modifier.size(24.dp))
        Icon(HedvigIcons.InfoOutline, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Link, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Lock, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Mic, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Minus, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Notification, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Pause, null, Modifier.size(24.dp))
        Icon(HedvigIcons.PaymentFilled, null, Modifier.size(24.dp))
        Icon(HedvigIcons.PaymentOutline, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Play, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Plus, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ProfileFilled, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ProfileOutline, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Refresh, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Reload, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Search, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Settings, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ShieldFilled, null, Modifier.size(24.dp))
        Icon(HedvigIcons.ShieldOutline, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Star, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Swap, null, Modifier.size(24.dp))
        Icon(HedvigIcons.Travel, null, Modifier.size(24.dp))
        Icon(HedvigIcons.WarningFilled, null, Modifier.size(24.dp))
        Icon(HedvigIcons.WarningOutline, null, Modifier.size(24.dp))
      }
    }
  }
}

@Preview
@Composable
private fun PreviewShowcaseIcons() {
  HedvigTheme {
    ShowcaseIcons()
  }
}
