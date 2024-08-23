package com.hedvig.android.sample.design.showcase.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigSnackbar
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface

@Composable
fun NotificationsSnackbarShowcase() {
  var showErrorSnack by remember { mutableStateOf(false) }
  var showCampaignSnack by remember { mutableStateOf(false) }
  var showWarningSnack by remember { mutableStateOf(false) }
  var showInfoSnack by remember { mutableStateOf(false) }
  var showNeutralSnack by remember { mutableStateOf(false) }

  Surface(
    modifier = Modifier
      .fillMaxSize(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Box(
      modifier = Modifier
        .safeContentPadding()
        .fillMaxSize(),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = "Error snack",
          enabled = true,
          onClick = {
            showErrorSnack = true
          },
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = "Info snack",
          enabled = true,
          onClick = {
            showInfoSnack = true
          },
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = "Neutral snack",
          enabled = true,
          onClick = {
            showNeutralSnack = true
          },
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = "Warning snack",
          enabled = true,
          onClick = {
            showWarningSnack = true
          },
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = "Campaign snack",
          enabled = true,
          onClick = {
            showCampaignSnack = true
          },
        )
      }
      HedvigSnackbar(
        snackbarText = "Code copied",
        priority = NotificationPriority.Info,
        actionLabel = "Action",
        showSnackbar = showInfoSnack,
        showedSnackbar = {
          showInfoSnack = false
        },
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .align(Alignment.BottomCenter),
      )
      HedvigSnackbar(
        snackbarText = "Could not connect to server Could not connect to serverCould not connect to server " +
          "Could not connect to server Could not connect to server Could not connect to server",
        priority = NotificationPriority.Error,
        showSnackbar = showErrorSnack,
        showedSnackbar = {
          showErrorSnack = false
        },
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .align(Alignment.BottomCenter),
      )
      HedvigSnackbar(
        snackbarText = "Campaign code added",
        priority = NotificationPriority.Campaign,
        showSnackbar = showCampaignSnack,
        showedSnackbar = {
          showCampaignSnack = false
        },
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .align(Alignment.BottomCenter),
      )
      HedvigSnackbar(
        snackbarText = "Something went wrong",
        priority = NotificationPriority.Attention,
        showSnackbar = showWarningSnack,
        showedSnackbar = {
          showWarningSnack = false
        },
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .align(Alignment.BottomCenter),
      )
      HedvigSnackbar(
        snackbarText = "Info goes here",
        priority = NotificationPriority.NeutralToast,
        showSnackbar = showNeutralSnack,
        showedSnackbar = { showNeutralSnack = false },
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .align(Alignment.BottomCenter),
      )
    }
  }
}
