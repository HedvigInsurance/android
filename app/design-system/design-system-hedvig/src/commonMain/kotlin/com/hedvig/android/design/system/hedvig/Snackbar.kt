package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.internals.InternalSnackBar
import com.hedvig.android.design.system.internals.SnackBarColors

@Composable
fun HedvigSnackbar(
  snackbarText: String,
  showSnackbar: Boolean,
  showedSnackbar: () -> Unit,
  modifier: Modifier = Modifier,
  priority: NotificationPriority = NotificationDefaults.defaultSnackbarPriority,
  action: (() -> Unit)? = null,
  actionLabel: String? = null,
) {
  InternalSnackBar(
    colors = priority.colors.let { notificationColors ->
      SnackBarColors(
        containerColor = notificationColors.containerColor,
        textColor = notificationColors.textColor,
        iconColor = notificationColors.iconColor,
      )
    },
    shape = NotificationDefaults.snackBarShape,
    snackbarText = snackbarText,
    showSnackbar = showSnackbar,
    showedSnackbar = showedSnackbar,
    modifier = modifier,
    action = action,
    actionLabel = actionLabel,
    textStyle = NotificationDefaults.textStyle,
    icon = {
      Icon(
        imageVector = priority.icon,
        contentDescription = EmptyContentDescription,
        tint = priority.colors.iconColor,
        modifier = Modifier.size(18.dp),
      )
    },
  )
}

@Composable
fun HedvigSnackBar(globalSnackBarState: GlobalSnackBarState, modifier: Modifier = Modifier) {
  val priority: NotificationPriority = globalSnackBarState.prio
  InternalSnackBar(
    colors = priority.colors.let { notificationColors ->
      SnackBarColors(
        containerColor = notificationColors.containerColor,
        textColor = notificationColors.textColor,
        iconColor = notificationColors.iconColor,
      )
    },
    shape = NotificationDefaults.snackBarShape,
    snackbarText = globalSnackBarState.text ?: "",
    showSnackbar = globalSnackBarState.show,
    showedSnackbar = globalSnackBarState::showedSnackBar,
    modifier = modifier,
    textStyle = NotificationDefaults.textStyle,
    icon = {
      Icon(
        imageVector = priority.icon,
        contentDescription = EmptyContentDescription,
        tint = priority.colors.iconColor,
        modifier = Modifier.size(18.dp),
      )
    },
  )
}

@Composable
fun rememberGlobalSnackBarState(): GlobalSnackBarState {
  return remember { GlobalSnackBarState() }
}

class GlobalSnackBarState {
  internal var show: Boolean by mutableStateOf(false)
    private set
  internal var text: String? by mutableStateOf(null)
    private set
  internal var prio: NotificationPriority by mutableStateOf(NotificationPriority.Info)
    private set

  fun show(text: String, prio: NotificationPriority = NotificationPriority.Info) {
    Snapshot.withMutableSnapshot {
      this.prio = prio
      this.text = text
      this.show = true
    }
  }

  internal fun showedSnackBar() {
    show = false
  }
}
