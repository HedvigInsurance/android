package com.hedvig.android.sample.design.showcase.ui.hedviguikit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.AndroidLogo
import com.hedvig.android.core.icons.hedvig.Apartment
import com.hedvig.android.core.icons.hedvig.AppleLogo
import com.hedvig.android.core.icons.hedvig.ArrowBack
import com.hedvig.android.core.icons.hedvig.ArrowDown
import com.hedvig.android.core.icons.hedvig.ArrowForward
import com.hedvig.android.core.icons.hedvig.ArrowUp
import com.hedvig.android.core.icons.hedvig.Basketball
import com.hedvig.android.core.icons.hedvig.Calendar
import com.hedvig.android.core.icons.hedvig.Camera
import com.hedvig.android.core.icons.hedvig.Certificate
import com.hedvig.android.core.icons.hedvig.ChevronDown
import com.hedvig.android.core.icons.hedvig.ChevronLeft
import com.hedvig.android.core.icons.hedvig.ChevronRight
import com.hedvig.android.core.icons.hedvig.ChevronUp
import com.hedvig.android.core.icons.hedvig.CircleWithCheckmark
import com.hedvig.android.core.icons.hedvig.CircleWithCheckmarkFilled
import com.hedvig.android.core.icons.hedvig.CircleWithPlus
import com.hedvig.android.core.icons.hedvig.CircleWithX
import com.hedvig.android.core.icons.hedvig.CircleWithXFilled
import com.hedvig.android.core.icons.hedvig.ContactInformation
import com.hedvig.android.core.icons.hedvig.Copy
import com.hedvig.android.core.icons.hedvig.Deductible
import com.hedvig.android.core.icons.hedvig.Document
import com.hedvig.android.core.icons.hedvig.Edit
import com.hedvig.android.core.icons.hedvig.Eurobonus
import com.hedvig.android.core.icons.hedvig.Heart
import com.hedvig.android.core.icons.hedvig.House
import com.hedvig.android.core.icons.hedvig.Info
import com.hedvig.android.core.icons.hedvig.InfoFilled
import com.hedvig.android.core.icons.hedvig.Language
import com.hedvig.android.core.icons.hedvig.Logout
import com.hedvig.android.core.icons.hedvig.Mail
import com.hedvig.android.core.icons.hedvig.MinusInCircle
import com.hedvig.android.core.icons.hedvig.MoreIos
import com.hedvig.android.core.icons.hedvig.MultipleDocuments
import com.hedvig.android.core.icons.hedvig.Other
import com.hedvig.android.core.icons.hedvig.Pause
import com.hedvig.android.core.icons.hedvig.Payments
import com.hedvig.android.core.icons.hedvig.Pictures
import com.hedvig.android.core.icons.hedvig.Play
import com.hedvig.android.core.icons.hedvig.Reciept
import com.hedvig.android.core.icons.hedvig.RestartOneArrow
import com.hedvig.android.core.icons.hedvig.RestartTwoArrows
import com.hedvig.android.core.icons.hedvig.Search
import com.hedvig.android.core.icons.hedvig.Settings
import com.hedvig.android.core.icons.hedvig.StopSign
import com.hedvig.android.core.icons.hedvig.StopSignFilled
import com.hedvig.android.core.icons.hedvig.Waiting
import com.hedvig.android.core.icons.hedvig.Warning
import com.hedvig.android.core.icons.hedvig.WarningFilled
import com.hedvig.android.core.icons.hedvig.Watch
import com.hedvig.android.core.icons.hedvig.X
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
  Column(Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState())) {
    for (iconsGroup in Icons.Hedvig.AllHedvigIcons) {
      FlowRow() {
        for (icon in iconsGroup) {
          Icon(icon, null)
        }
      }
    }
  }
}

private var __AllHedvigIcons: List<List<ImageVector>>? = null

private val Hedvig.AllHedvigIcons: List<List<ImageVector>>
  get() {
    if (__AllHedvigIcons != null) {
      return __AllHedvigIcons!!
    }
    __AllHedvigIcons = listOf(
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
        Payments,
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
    )
    return __AllHedvigIcons!!
  }
