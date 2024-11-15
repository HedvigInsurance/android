package com.hedvig.android.feature.claim.details.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant

@Composable
internal fun SubmittedAndClosedColumns(submittedAt: Instant, closedAt: Instant?, locale: Locale) {
  val now by currentTimeAsState(updateInterval = 1.seconds)
  val submittedText by remember(submittedAt) {
    derivedStateOf { HedvigDateUtils.getRelativeTimeSpanString(submittedAt, now) }
  }
  val closedText by remember(closedAt) {
    derivedStateOf {
      if (closedAt == null) {
        "—"
      } else {
        HedvigDateUtils.getRelativeTimeSpanString(closedAt, now)
      }
    }
  }
  Row {
    SubmittedAndClosedColumn(
      topText = stringResource(hedvig.resources.R.string.claim_status_detail_submitted).uppercase(locale),
      bottomText = submittedText,
      modifier = Modifier.weight(0.5f),
    )
    SubmittedAndClosedColumn(
      topText = stringResource(hedvig.resources.R.string.claim_status_detail_closed).uppercase(locale),
      bottomText = closedText,
      modifier = Modifier.weight(0.5f),
    )
  }
}

@Composable
private fun SubmittedAndClosedColumn(topText: String, bottomText: String, modifier: Modifier = Modifier) {
  Column(
    verticalArrangement = Arrangement.spacedBy(4.dp),
    modifier = modifier,
  ) {
    HedvigText(
      text = topText,
      style = HedvigTheme.typography.label.copy(color = HedvigTheme.colorScheme.textSecondary),
    )
    HedvigText(
      text = bottomText,
      style = HedvigTheme.typography.bodySmall,
    )
  }
}

@Composable
private fun currentTimeAsState(updateInterval: Duration = 1.seconds, clock: Clock = Clock.System): State<Instant> {
  return produceState(initialValue = clock.now()) {
    while (isActive) {
      delay(updateInterval)
      value = clock.now()
    }
  }
}

private object HedvigDateUtils {
  /**
   * Returns a localized string, like:
   * 7 or more days ago  -> English: Dec 16, 2021   Swedish: 16 dec. 2021
   * 6 days ago          -> English: 6 days ago     Swedish: För 6 dagar sedan
   * 3 days ago          -> English: 3 days ago     Swedish: För 3 dagar sedan
   * 2 days ago          -> English: 2 days ago     Swedish: I förrgår
   * 1 day ago           -> English: Yesterday      Swedish: I går
   * 10 hours ago        -> English: 10 hours ago   Swedish: För 10 timmar sedan
   * 30 minutes ago      -> English: 30 minutes ago Swedish: För 30 minuter sedan
   * 30 seconds ago      -> English: 30 seconds ago Swedish: För 30 sekunder sedan
   *
   * DateUtils look at locale from what was set at Configuration(context.resources.configuration).setLocale()
   * Also looks at what was set with Locale.setDefault() so if that is called without changing the local
   * configuration it *will* show the wrong locale compared to the rest of the screen, so we have to make sure those
   * two are the same.
   */
  fun getRelativeTimeSpanString(from: Instant, to: Instant): String {
    return DateUtils.getRelativeTimeSpanString(
      from.toEpochMilliseconds(),
      to.toEpochMilliseconds(),
      DateUtils.SECOND_IN_MILLIS,
    ).toString()
  }
}

@HedvigPreview
@Preview(locale = "sv")
@Preview(locale = "nn")
@Preview(locale = "da")
@Preview(locale = "fr")
@Preview(locale = "el")
@Composable
private fun PreviewSubmittedAndClosedInformation() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SubmittedAndClosedColumns(
        submittedAt = System.now().minus(10.days),
        closedAt = System.now().minus(30.seconds),
        locale = Locale.ENGLISH,
      )
    }
  }
}
