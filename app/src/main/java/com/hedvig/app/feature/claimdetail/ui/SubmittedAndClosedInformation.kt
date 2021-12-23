package com.hedvig.app.feature.claimdetail.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.HedvigDateUtils
import com.hedvig.app.util.compose.currentTimeAsState
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.TimeZone

@Composable
fun SubmittedAndClosedInformation(
    submittedAt: Instant,
    closedAt: Instant?,
    locale: Locale,
) {
    val now by currentTimeAsState(updateIntervalInSeconds = 1L)
    val submittedText by remember(submittedAt) {
        derivedStateOf { HedvigDateUtils.getRelativeTimeSpanString(submittedAt, now) }
    }
    val closedText by remember(submittedAt) {
        derivedStateOf {
            if (closedAt == null) {
                "—"
            } else {
                HedvigDateUtils.getRelativeTimeSpanString(closedAt, now)
            }
        }
    }
    Row {
        SubmittedAndClosedInformationColumn(
            topText = stringResource(R.string.claim_status_detail_submitted).uppercase(locale),
            bottomText = submittedText,
            modifier = Modifier.weight(0.5f)
        )
        SubmittedAndClosedInformationColumn(
            topText = stringResource(R.string.claim_status_detail_closed).uppercase(locale),
            bottomText = closedText,
            modifier = Modifier.weight(0.5f)
        )
    }
}

@Composable
private fun SubmittedAndClosedInformationColumn(
    topText: String,
    bottomText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = topText,
                style = MaterialTheme.typography.caption,
            )
        }
        Text(
            text = bottomText,
            style = MaterialTheme.typography.body1,
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(locale = "sv")
@Preview(locale = "nn")
@Preview(locale = "da")
@Preview(locale = "fr")
@Preview(locale = "el")
@Composable
fun SubmittedAndClosedInformationPreview() {
    TimeZone.getDefault().id
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            SubmittedAndClosedInformation(
                submittedAt = Instant.now().minus(Duration.ofDays(10)),
                closedAt = Instant.now().minus(Duration.ofSeconds(30)),
                Locale.ENGLISH,
            )
        }
    }
}
