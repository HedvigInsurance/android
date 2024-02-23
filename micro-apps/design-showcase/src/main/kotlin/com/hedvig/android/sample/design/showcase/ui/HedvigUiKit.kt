package com.hedvig.android.sample.design.showcase.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.PermissionStatus
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.SignedAudioUrl
import com.hedvig.android.audio.player.state.PlayableAudioSource
import com.hedvig.android.audio.player.state.rememberAudioPlayer
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.information.AppStateInformation
import com.hedvig.android.core.designsystem.component.information.AppStateInformationType
import com.hedvig.android.core.designsystem.component.success.HedvigSuccessSection
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.card.ExpandablePlusCard
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.dialog.HedvigAlertDialog
import com.hedvig.android.core.ui.dialog.SingleSelectDialog
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.ui.MemberReminderCards
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.sample.design.showcase.ui.hedviguikit.HedvigIcons
import com.hedvig.android.sample.design.showcase.ui.temp.ClaimProgressUiState
import com.hedvig.android.sample.design.showcase.ui.temp.ClaimStatusCard
import com.hedvig.android.sample.design.showcase.ui.temp.ClaimStatusCardUiState
import com.hedvig.android.sample.design.showcase.ui.temp.PillUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun HedvigUiKit() {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .clearFocusOnTap(),
    contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
  ) {
    item {
      Text(
        text = "Hedvig UI Kit",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(24.dp),
      )
    }
    lightAndDarkItem {
      ClaimStatusCard(
        ClaimStatusCardUiState(
          "1",
          PillUiState.PillType.entries.map {
            PillUiState(it.name, it)
          },
          "Title",
          "Subtitle",
          ClaimProgressUiState.ClaimProgressType.entries.dropLast(1).map {
            ClaimProgressUiState(it.name, it)
          },
        ),
        {},
      )
    }
    lightAndDarkItem {
      MemberReminderCards(
        memberReminders = persistentListOf(
          MemberReminder.ConnectPayment(),
          MemberReminder.UpcomingRenewal(
            contractDisplayName = "ContractDisplayName",
            renewalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
            draftCertificateUrl = "",
          ),
          MemberReminder.EnableNotifications(),
        ),
        navigateToConnectPayment = {},
        openUrl = {},
        notificationPermissionState = object : NotificationPermissionState {
          override val showDialog = false

          override fun dismissDialog() {}

          override fun launchPermissionRequest() {}

          override val permission: String
            get() = ""
          override val status: PermissionStatus
            get() = PermissionStatus.Granted
        },
        snoozeNotificationPermissionReminder = {},
        navigateToAddMissingInfo = {},
        contentPadding = PaddingValues(16.dp),
      )
    }
    lightAndDarkItem {
      HedvigCard(Modifier.fillMaxWidth()) { Text("HedvigCard", Modifier.padding(16.dp)) }
    }
    lightAndDarkItem {
      HedvigInfoCard(Modifier.fillMaxWidth()) { Text("HedvigInfoCard", Modifier.padding(16.dp)) }
    }
    lightAndDarkItem {
      InsuranceCard(
        chips = persistentListOf("Chip#1", "Chip#2"),
        topText = "Top text",
        bottomText = "Bottom text",
        imageLoader = rememberPreviewImageLoader(),
      )
    }
    lightAndDarkItem {
      VectorInfoCard("VectorInfoCard")
    }
    lightAndDarkItem {
      HedvigDatePicker(rememberDatePickerState())
    }
    lightAndDarkItem {
      val audioPlayer = rememberAudioPlayer(
        PlayableAudioSource.RemoteUrl(
          SignedAudioUrl.fromSignedAudioUrlString(
            "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
          ),
        ),
      )
      HedvigAudioPlayer(audioPlayer) { audioPlayer.startPlayer() }
    }
    lightAndDarkItem {
      var text by remember { mutableStateOf("HedvigTextField") }
      HedvigTextField(text, { text = it })
    }
    lightAndDarkItem {
      var text by remember { mutableStateOf("HedvigTextField") }
      HedvigTextField(text, { text = it })
    }
    lightAndDarkItem {
      var expanded by remember { mutableStateOf(false) }
      ExpandablePlusCard(
        isExpanded = expanded,
        onClick = { expanded = !expanded },
        content = { Text("Content", Modifier.weight(1f)) },
        expandedContent = { Text("Expanded Content".repeat(15), Modifier.padding(32.dp)) },
        Modifier.fillMaxWidth(),
      )
    }
    lightAndDarkItem {
      HedvigContainedButton("HedvigContainedButton", {})
    }
    lightAndDarkItem {
      HedvigTextButton("HedvigTextButton", {})
    }
    lightAndDarkItem {
      HedvigContainedButton(text = "", onClick = { }, isLoading = true)
    }
    lightAndDarkItem {
      Column {
        for (appStateInformationType in AppStateInformationType.entries) {
          AppStateInformation(appStateInformationType, appStateInformationType.toString(), "AppStateInformationType")
        }
      }
    }
    lightAndDarkItem {
      HedvigErrorSection(retry = { })
    }
    lightAndDarkItem {
      HedvigSuccessSection("HedvigSuccessSection")
    }

    item {
      Text(
        text = "Dialogs",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(24.dp),
      )
    }
    lightAndDarkItem {
      var showDialog by remember { mutableStateOf(false) }
      HedvigContainedButton(text = "show error dialog", onClick = { showDialog = true })
      if (showDialog) {
        ErrorDialog(message = "Error dialog", onDismiss = { showDialog = false })
      }
    }
    lightAndDarkItem {
      var showDialog by remember { mutableStateOf(false) }
      HedvigContainedButton(text = "show error dialog", onClick = { showDialog = true })
      if (showDialog) {
        HedvigAlertDialog(
          title = "Title",
          text = "text",
          onDismissRequest = { showDialog = false },
          onConfirmClick = { showDialog = false },
        )
      }
    }
    lightAndDarkItem {
      var showDialog by remember { mutableStateOf(false) }
      HedvigContainedButton(text = "show error dialog", onClick = { showDialog = true })
      if (showDialog) {
        SingleSelectDialog(
          title = "Title",
          optionsList = listOf("#1", "#2"),
          onSelected = {},
          getDisplayText = { it },
          getIsSelected = { it == "#1" },
          getId = { it },
          onDismissRequest = { showDialog = false },
        )
      }
    }
    lightAndDarkItem {
      HedvigIcons()
    }
  }
}

private fun LazyListScope.lightAndDarkItem(content: @Composable LazyItemScope.() -> Unit) {
  item {
    LADCOntainer { content() }
  }
}

@Composable
private inline fun LADCOntainer(crossinline content: @Composable () -> Unit) {
  val darkTheme = isSystemInDarkTheme()
  var isDarkTheme by remember(darkTheme) { mutableStateOf(darkTheme) }
  HedvigTheme(darkTheme = isDarkTheme) {
    Surface(
      color = MaterialTheme.colorScheme.background,
    ) {
      Column {
        Switch(
          checked = isDarkTheme,
          onCheckedChange = { isDarkTheme = it },
          modifier = Modifier
            .align(Alignment.End)
            .padding(8.dp),
        )
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.padding(16.dp),
        ) {
          content()
        }
        HorizontalDivider()
      }
    }
  }
}
