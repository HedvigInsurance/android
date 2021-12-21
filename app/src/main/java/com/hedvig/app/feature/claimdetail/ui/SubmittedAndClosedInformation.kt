package com.hedvig.app.feature.claimdetail.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import java.time.Instant
import java.util.Locale

@Composable
fun SubmittedAndClosedInformation(
    submittedAt: Instant,
    closedAt: Instant?,
    locale: Locale,
) {
    val context = LocalContext.current
    val submittedText = remember(submittedAt) {
        DateUtils.getRelativeTimeSpanString(context, submittedAt.toEpochMilli()).toString()
    }
    val closedText = remember(closedAt) {
        if (closedAt == null) {
            "—"
        } else {
            DateUtils.getRelativeTimeSpanString(context, closedAt.toEpochMilli()).toString()
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
