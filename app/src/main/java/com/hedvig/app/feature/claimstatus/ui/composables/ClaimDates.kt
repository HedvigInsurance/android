package com.hedvig.app.feature.claimstatus.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun ClaimDates(
    submittedAt: Instant,
    closedAt: Instant?,
    locale: Locale,
) {
    val submittedAtText = remember(submittedAt) {
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
            .format(submittedAt)
    }
    val closedAtText = remember(closedAt) {
        if (closedAt == null) {
            "—"
        } else {
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault())
                .format(closedAt)
        }
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TitleAndTimeColumn(
            title = stringResource(R.string.claim_status_bar_submitted),
            time = submittedAtText,
            locale = locale,
            horizontalAlignment = Alignment.Start
        )
        TitleAndTimeColumn(
            title = stringResource(R.string.claim_status_bar_closed),
            time = closedAtText,
            locale = locale,
            horizontalAlignment = Alignment.End
        )
    }
}

@Composable
private fun TitleAndTimeColumn(
    title: String,
    time: String,
    locale: Locale,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = title.uppercase(locale),
                style = MaterialTheme.typography.overline
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.body2
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClaimDatesPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimDates(
                submittedAt = Instant.now().minus(10, ChronoUnit.DAYS),
                closedAt = Instant.now().minus(1, ChronoUnit.DAYS),
                locale = Locale.forLanguageTag("en_SE")
            )
        }
    }
}
