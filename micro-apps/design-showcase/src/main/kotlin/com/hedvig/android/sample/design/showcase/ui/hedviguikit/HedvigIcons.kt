package com.hedvig.android.sample.design.showcase.ui.hedviguikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.HedvigIcons
import com.hedvig.android.core.icons.hedvig.colored.hedvig.Chat
import com.hedvig.android.core.icons.hedvig.colored.hedvig.ColoredCircleWithCampaign
import com.hedvig.android.core.icons.hedvig.colored.hedvig.FirstVet
import com.hedvig.android.core.icons.hedvig.compose.notificationCircle
import com.hedvig.android.core.icons.hedvig.compose.notificationCircleWithSubtractingPadding
import com.hedvig.android.core.icons.hedvig.flag.FlagDenmark
import com.hedvig.android.core.icons.hedvig.flag.FlagNorway
import com.hedvig.android.core.icons.hedvig.flag.FlagSweden
import com.hedvig.android.core.icons.hedvig.flag.FlagUk
import com.hedvig.android.core.icons.hedvig.logo.HedvigLogotype
import com.hedvig.android.core.icons.hedvig.nav.Payments
import com.hedvig.android.core.icons.hedvig.nav.PaymentsFilled
import com.hedvig.android.core.icons.hedvig.nav.hedvig.Forever
import com.hedvig.android.core.icons.hedvig.nav.hedvig.ForeverFilled
import com.hedvig.android.core.icons.hedvig.nav.hedvig.Home
import com.hedvig.android.core.icons.hedvig.nav.hedvig.HomeFilled
import com.hedvig.android.core.icons.hedvig.nav.hedvig.Insurance
import com.hedvig.android.core.icons.hedvig.nav.hedvig.InsuranceFilled
import com.hedvig.android.core.icons.hedvig.nav.hedvig.Profile
import com.hedvig.android.core.icons.hedvig.nav.hedvig.ProfileFilled
import com.hedvig.android.core.icons.hedvig.normal.AndroidLogo
import com.hedvig.android.core.icons.hedvig.normal.Apartment
import com.hedvig.android.core.icons.hedvig.normal.AppleLogo
import com.hedvig.android.core.icons.hedvig.normal.ArrowBack
import com.hedvig.android.core.icons.hedvig.normal.ArrowDown
import com.hedvig.android.core.icons.hedvig.normal.ArrowForward
import com.hedvig.android.core.icons.hedvig.normal.ArrowUp
import com.hedvig.android.core.icons.hedvig.normal.Basketball
import com.hedvig.android.core.icons.hedvig.normal.Calendar
import com.hedvig.android.core.icons.hedvig.normal.Camera
import com.hedvig.android.core.icons.hedvig.normal.Certificate
import com.hedvig.android.core.icons.hedvig.normal.ChevronDown
import com.hedvig.android.core.icons.hedvig.normal.ChevronLeft
import com.hedvig.android.core.icons.hedvig.normal.ChevronRight
import com.hedvig.android.core.icons.hedvig.normal.ChevronUp
import com.hedvig.android.core.icons.hedvig.normal.CircleWithCheckmark
import com.hedvig.android.core.icons.hedvig.normal.CircleWithCheckmarkFilled
import com.hedvig.android.core.icons.hedvig.normal.CircleWithPlus
import com.hedvig.android.core.icons.hedvig.normal.CircleWithX
import com.hedvig.android.core.icons.hedvig.normal.CircleWithXFilled
import com.hedvig.android.core.icons.hedvig.normal.ContactInformation
import com.hedvig.android.core.icons.hedvig.normal.Copy
import com.hedvig.android.core.icons.hedvig.normal.Deductible
import com.hedvig.android.core.icons.hedvig.normal.Document
import com.hedvig.android.core.icons.hedvig.normal.Edit
import com.hedvig.android.core.icons.hedvig.normal.Eurobonus
import com.hedvig.android.core.icons.hedvig.normal.Heart
import com.hedvig.android.core.icons.hedvig.normal.House
import com.hedvig.android.core.icons.hedvig.normal.Info
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.icons.hedvig.normal.Language
import com.hedvig.android.core.icons.hedvig.normal.Logout
import com.hedvig.android.core.icons.hedvig.normal.Mail
import com.hedvig.android.core.icons.hedvig.normal.MinusInCircle
import com.hedvig.android.core.icons.hedvig.normal.MoreIos
import com.hedvig.android.core.icons.hedvig.normal.MultipleDocuments
import com.hedvig.android.core.icons.hedvig.normal.Other
import com.hedvig.android.core.icons.hedvig.normal.Pause
import com.hedvig.android.core.icons.hedvig.normal.Pictures
import com.hedvig.android.core.icons.hedvig.normal.Play
import com.hedvig.android.core.icons.hedvig.normal.Reciept
import com.hedvig.android.core.icons.hedvig.normal.RestartOneArrow
import com.hedvig.android.core.icons.hedvig.normal.RestartTwoArrows
import com.hedvig.android.core.icons.hedvig.normal.Search
import com.hedvig.android.core.icons.hedvig.normal.Settings
import com.hedvig.android.core.icons.hedvig.normal.StopSign
import com.hedvig.android.core.icons.hedvig.normal.StopSignFilled
import com.hedvig.android.core.icons.hedvig.normal.Waiting
import com.hedvig.android.core.icons.hedvig.normal.Warning
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.icons.hedvig.normal.Watch
import com.hedvig.android.core.icons.hedvig.normal.X
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.core.icons.hedvig.small.hedvig.BankId
import com.hedvig.android.core.icons.hedvig.small.hedvig.Campaign
import com.hedvig.android.core.icons.hedvig.small.hedvig.Checkmark
import com.hedvig.android.core.icons.hedvig.small.hedvig.CircleFilled
import com.hedvig.android.core.icons.hedvig.small.hedvig.CircleWithOutline
import com.hedvig.android.core.icons.hedvig.small.hedvig.Lock
import com.hedvig.android.core.icons.hedvig.small.hedvig.Minus
import com.hedvig.android.core.icons.hedvig.small.hedvig.Plus
import com.hedvig.android.core.icons.hedvig.small.hedvig.Sound
import com.hedvig.android.core.icons.hedvig.small.hedvig.SquircleWithCheckmark

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HedvigIcons() {
  Column(
    Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .safeContentPadding(),
  ) {
    for ((index, iconsGroup) in Icons.Hedvig.AllHedvigIcons.withIndex()) {
      FlowRow {
        for (icon in iconsGroup) {
          val modifier = Modifier
          if (index <= 1) { // #0 and #1 are the flags and colored icons which should not be tinted
            Image(icon, null, modifier)
            if (index == 1) {
              Image(icon, null, modifier.notificationCircle())
            }
          } else {
            Icon(icon, null, modifier)
            if (index == 2) {
              Icon(icon, null, modifier.notificationCircleWithSubtractingPadding())
            }
          }
        }
      }
    }
  }
}

private var __AllHedvigIcons: List<List<ImageVector>>? = null

private val HedvigIcons.AllHedvigIcons: List<List<ImageVector>>
  get() {
    if (__AllHedvigIcons != null) {
      return __AllHedvigIcons!!
    }
    __AllHedvigIcons = listOf(
      listOf(
        FlagDenmark,
        FlagNorway,
        FlagSweden,
        FlagUk,
      ),
      listOf(
        ColoredCircleWithCampaign,
        Chat,
        FirstVet,
      ),
      // Nav icons
      listOf(
        Home,
        HomeFilled,
        Insurance,
        InsuranceFilled,
        Forever,
        ForeverFilled,
        Payments,
        PaymentsFilled,
        Profile,
        ProfileFilled,
      ),
      listOf(
        AndroidLogo,
        Apartment,
        AppleLogo,
        ArrowBack,
        ArrowDown,
        ArrowForward,
        ArrowUp,
        Basketball,
        Calendar,
        Camera,
        Certificate,
        ChevronDown,
        ChevronLeft,
        ChevronRight,
        ChevronUp,
        CircleWithCheckmark,
        CircleWithCheckmarkFilled,
        CircleWithPlus,
        CircleWithX,
        CircleWithXFilled,
        ContactInformation,
        Copy,
        Deductible,
        Document,
        Edit,
        Eurobonus,
        Heart,
        House,
        Info,
        InfoFilled,
        Language,
        Logout,
        Mail,
        MinusInCircle,
        MoreIos,
        MultipleDocuments,
        Other,
        Pause,
        Pictures,
        Play,
        Reciept,
        RestartOneArrow,
        RestartTwoArrows,
        Search,
        Settings,
        StopSign,
        StopSignFilled,
        Waiting,
        Warning,
        WarningFilled,
        Watch,
        X,
      ),
      // Small icons
      listOf(
        ArrowNorthEast,
        BankId,
        Campaign,
        Checkmark,
        CircleFilled,
        CircleWithOutline,
        Lock,
        Minus,
        Plus,
        Sound,
        SquircleWithCheckmark,
      ),
      // logotype
      listOf(
        HedvigLogotype,
      ),
    )
    return __AllHedvigIcons!!
  }

@HedvigPreview
@Composable
private fun PreviewHedvigIcons() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigIcons()
    }
  }
}
