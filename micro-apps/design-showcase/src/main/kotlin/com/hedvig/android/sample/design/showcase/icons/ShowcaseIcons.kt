package com.hedvig.android.sample.design.showcase.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredCampaign
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredCampaignWithDot
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredChat
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredChatWithDot
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredFirstVet
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredFirstVetWithDot
import com.hedvig.android.design.system.hedvig.icon.flag.FlagDenmark
import com.hedvig.android.design.system.hedvig.icon.flag.FlagNorway
import com.hedvig.android.design.system.hedvig.icon.flag.FlagSweden
import com.hedvig.android.design.system.hedvig.icon.flag.FlagUk

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ShowcaseIcons(modifier: Modifier = Modifier) {
  CompositionLocalProvider(LocalContentColor provides Color.Black) {
    Column(modifier) {
      FlowRow {
        Icon(HedvigIcons.ArrowDown, null)
        Icon(HedvigIcons.ArrowLeft, null)
        Icon(HedvigIcons.ArrowNorthEast, null)
        Icon(HedvigIcons.ArrowRight, null)
        Icon(HedvigIcons.ArrowUp, null)
        Icon(HedvigIcons.Attach, null)
        Icon(HedvigIcons.AttentionFilled, null)
        Icon(HedvigIcons.AttentionOutline, null)
        Icon(HedvigIcons.Camera, null)
        Icon(HedvigIcons.Campaign, null)
        Icon(HedvigIcons.Card, null)
        Icon(HedvigIcons.Cart, null)
        Icon(HedvigIcons.CartAdded, null)
        Icon(HedvigIcons.Certified, null)
        Icon(HedvigIcons.Chat, null)
        Icon(HedvigIcons.CheckFilled, null)
        Icon(HedvigIcons.Checkmark, null)
        Icon(HedvigIcons.CheckOutline, null)
        Icon(HedvigIcons.ChevronDown, null)
        Icon(HedvigIcons.ChevronDownSmall, null)
        Icon(HedvigIcons.ChevronLeft, null)
        Icon(HedvigIcons.ChevronLeftSmall, null)
        Icon(HedvigIcons.ChevronRight, null)
        Icon(HedvigIcons.ChevronRightSmall, null)
        Icon(HedvigIcons.ChevronUp, null)
        Icon(HedvigIcons.ChevronUpSmall, null)
        Icon(HedvigIcons.CircleFilled, null)
        Icon(HedvigIcons.CircleOutline, null)
        Icon(HedvigIcons.Clock, null)
        Icon(HedvigIcons.Close, null)
        Icon(HedvigIcons.Copy, null)
        Icon(HedvigIcons.Document, null)
        Icon(HedvigIcons.Dots, null)
        Icon(HedvigIcons.Download, null)
        Icon(HedvigIcons.EQ, null)
        Icon(HedvigIcons.HelipadFilled, null)
        Icon(HedvigIcons.HelipadOutline, null)
        Icon(HedvigIcons.ID, null)
        Icon(HedvigIcons.Image, null)
        Icon(HedvigIcons.InfoFilled, null)
        Icon(HedvigIcons.InfoOutline, null)
        Icon(HedvigIcons.Link, null)
        Icon(HedvigIcons.Lock, null)
        Icon(HedvigIcons.Mic, null)
        Icon(HedvigIcons.Minus, null)
        Icon(HedvigIcons.Notification, null)
        Icon(HedvigIcons.Pause, null)
        Icon(HedvigIcons.PaymentFilled, null)
        Icon(HedvigIcons.PaymentOutline, null)
        Icon(HedvigIcons.Play, null)
        Icon(HedvigIcons.Plus, null)
        Icon(HedvigIcons.ProfileFilled, null)
        Icon(HedvigIcons.ProfileOutline, null)
        Icon(HedvigIcons.Refresh, null)
        Icon(HedvigIcons.Reload, null)
        Icon(HedvigIcons.Search, null)
        Icon(HedvigIcons.Settings, null)
        Icon(HedvigIcons.ShieldFilled, null)
        Icon(HedvigIcons.ShieldOutline, null)
        Icon(HedvigIcons.Star, null)
        Icon(HedvigIcons.Swap, null)
        Icon(HedvigIcons.Travel, null)
        Icon(HedvigIcons.WarningFilled, null)
        Icon(HedvigIcons.WarningOutline, null)
      }
      FlowRow {
        Image(HedvigIcons.FlagDenmark, null)
        Image(HedvigIcons.FlagNorway, null)
        Image(HedvigIcons.FlagSweden, null)
        Image(HedvigIcons.FlagUk, null)
      }
      FlowRow {
        Image(HedvigIcons.ColoredCampaign, null)
        Image(HedvigIcons.ColoredCampaignWithDot, null)
        Image(HedvigIcons.ColoredChat, null)
        Image(HedvigIcons.ColoredChatWithDot, null)
        Image(HedvigIcons.ColoredFirstVet, null)
        Image(HedvigIcons.ColoredFirstVetWithDot, null)
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
