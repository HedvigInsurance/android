package com.hedvig.android.feature.importantmessages.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.importantmessages.PublicImportantMessage
import com.hedvig.android.data.importantmessages.PublicImportantMessage.Link
import com.hedvig.android.data.importantmessages.PublicImportantMessage.Severity
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
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

/**
 * Asks for a reload when [reloadKey] changes and on every resume except the screen's first one, which is remembered
 * across recreation. The presenter also loads by itself whenever it starts, so a resume that follows a presenter
 * restart loads twice, and the second load replaces the first.
 */
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

/**
 * One card per message, fading and growing in when the first messages arrive and out when the last ones go. With no
 * messages it takes no space at all.
 */
@Composable
internal fun PublicImportantMessageCards(
  messages: List<PublicImportantMessage>,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var lastShownMessages by remember { mutableStateOf(messages) }
  LaunchedEffect(messages) {
    if (messages.isNotEmpty()) lastShownMessages = messages
  }
  AnimatedVisibility(
    visible = messages.isNotEmpty(),
    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
    modifier = modifier,
  ) {
    val linkClickLabel = stringResource(Res.string.TALKBACK_OPEN_EXTERNAL_LINK)
    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      for (message in messages.ifEmpty { lastShownMessages }) {
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
}

@Preview
@Composable
private fun PreviewPublicImportantMessageInfo() {
  PreviewCards(previewMessage(Severity.Info, link = Link("https://hedvig.com", "Read more")))
}

@Preview
@Composable
private fun PreviewPublicImportantMessageWarning() {
  PreviewCards(previewMessage(Severity.Warning))
}

@Preview
@Composable
private fun PreviewPublicImportantMessageError() {
  PreviewCards(previewMessage(Severity.Error, link = Link("https://hedvig.com", "See status")))
}

@Preview
@Composable
private fun PreviewPublicImportantMessagesStacked() {
  PreviewCards(
    previewMessage(Severity.Error, link = Link("https://hedvig.com", "See status")),
    previewMessage(Severity.Info),
  )
}

/** In the light theme only, as the login screen that shows these cards is always light. */
@Composable
private fun PreviewCards(vararg messages: PublicImportantMessage) {
  HedvigTheme(darkTheme = false) {
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
