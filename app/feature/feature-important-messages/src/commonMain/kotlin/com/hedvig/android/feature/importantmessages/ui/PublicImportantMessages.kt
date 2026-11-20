package com.hedvig.android.feature.importantmessages.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.importantmessages.PublicImportantMessage
import com.hedvig.android.data.importantmessages.PublicImportantMessage.Link
import com.hedvig.android.data.importantmessages.PublicImportantMessage.Severity
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.importantmessages.PublicImportantMessagesEvent
import com.hedvig.android.feature.importantmessages.PublicImportantMessagesViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import hedvig.resources.Res
import hedvig.resources.TALKBACK_OPEN_EXTERNAL_LINK
import org.jetbrains.compose.resources.stringResource

/**
 * The important messages for people who are not logged in, one card per message, or nothing when there are none.
 * Loads them again when [reloadKey] changes, such as on a language change, and whenever the screen resumes.
 */
@Composable
fun PublicImportantMessages(reloadKey: Any?, openUrl: (String) -> Unit, modifier: Modifier = Modifier) {
  val viewModel: PublicImportantMessagesViewModel = metroViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ReloadOnKeyChangeAndResume(reloadKey = reloadKey, reload = { viewModel.emit(PublicImportantMessagesEvent.Reload) })
  PublicImportantMessageCards(messages = uiState.messages, openUrl = openUrl, modifier = modifier)
}

/** The presenter loads once when it starts, so neither the first composition nor the first resume asks again. */
@Composable
internal fun ReloadOnKeyChangeAndResume(reloadKey: Any?, reload: () -> Unit) {
  var loadedReloadKey by remember { mutableStateOf(reloadKey) }
  LaunchedEffect(reloadKey) {
    if (reloadKey != loadedReloadKey) {
      loadedReloadKey = reloadKey
      reload()
    }
  }
  var hasResumedOnce by rememberSaveable { mutableStateOf(false) }
  LifecycleResumeEffect(Unit) {
    if (hasResumedOnce) {
      reload()
    } else {
      hasResumedOnce = true
    }
    onPauseOrDispose {}
  }
}

@Composable
private fun PublicImportantMessageCards(
  messages: List<PublicImportantMessage>,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (messages.isEmpty()) return
  val linkClickLabel = stringResource(Res.string.TALKBACK_OPEN_EXTERNAL_LINK)
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    for (message in messages) {
      HedvigNotificationCard(
        message = message.message,
        priority = when (message.severity) {
          Severity.Info -> NotificationPriority.Info
          Severity.Warning -> NotificationPriority.Attention
          Severity.Error -> NotificationPriority.Error
        },
        modifier = Modifier.fillMaxWidth(),
        style = message.link?.let { link ->
          InfoCardStyle.Button(
            buttonText = link.buttonText,
            onButtonClick = { openUrl(link.url) },
            onButtonClickLabel = linkClickLabel,
          )
        } ?: InfoCardStyle.Default,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPublicImportantMessageInfo() {
  PreviewCards(previewMessage(Severity.Info, link = Link("https://hedvig.com", "Read more")))
}

@HedvigPreview
@Composable
private fun PreviewPublicImportantMessageWarning() {
  PreviewCards(previewMessage(Severity.Warning))
}

@HedvigPreview
@Composable
private fun PreviewPublicImportantMessageError() {
  PreviewCards(previewMessage(Severity.Error, link = Link("https://hedvig.com", "See status")))
}

@HedvigPreview
@Composable
private fun PreviewPublicImportantMessagesStacked() {
  PreviewCards(
    previewMessage(Severity.Error, link = Link("https://hedvig.com", "See status")),
    previewMessage(Severity.Info),
  )
}

@Composable
private fun PreviewCards(vararg messages: PublicImportantMessage) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PublicImportantMessageCards(messages = messages.toList(), openUrl = {}, modifier = Modifier.padding(16.dp))
    }
  }
}

private fun previewMessage(severity: Severity, link: Link? = null) = PublicImportantMessage(
  id = severity.name,
  message = "Logging in with BankID is slower than usual. We are working on it.",
  severity = severity,
  link = link,
)
