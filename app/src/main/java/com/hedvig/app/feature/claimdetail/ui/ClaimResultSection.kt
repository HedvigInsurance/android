package com.hedvig.app.feature.claimdetail.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.feature.claimdetail.model.ClaimDetailResult
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusColors
import com.hedvig.app.ui.compose.composables.pill.Pill
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.apollo.formatOnlyNumber
import com.hedvig.app.util.compose.preview.PreviewData
import java.util.Locale

@Composable
fun ClaimResultSection(
    claimDetailResult: ClaimDetailResult.Closed,
    locale: Locale,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when (claimDetailResult) {
            ClaimDetailResult.Closed.NotCompensated -> Pill(
                stringResource(R.string.claim_decision_not_compensated).uppercase(locale),
                backgroundColor = MaterialTheme.colors.primary
            )
            ClaimDetailResult.Closed.NotCovered -> Pill(
                stringResource(R.string.claim_decision_not_covered).uppercase(locale),
                backgroundColor = MaterialTheme.colors.primary
            )
            is ClaimDetailResult.Closed.Paid -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Pill(
                        stringResource(R.string.claim_decision_paid).uppercase(locale),
                        backgroundColor = ClaimStatusColors.Pill.paid
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        claimDetailResult.monetaryAmount.formatOnlyNumber(locale),
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(Modifier.width(2.dp))
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            claimDetailResult.monetaryAmount.currency.currencyCode,
                            style = MaterialTheme.typography.subtitle2,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClaimResultSectionPreview(
    @PreviewParameter(ClaimDetailResultProvider::class) result: ClaimDetailResult.Closed,
) {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimResultSection(result, Locale.getDefault())
        }
    }
}

class ClaimDetailResultProvider : CollectionPreviewParameterProvider<ClaimDetailResult.Closed>(
    listOf(
        ClaimDetailResult.Closed.Paid(PreviewData.monetaryAmount(2_500.00)),
        ClaimDetailResult.Closed.NotCompensated,
        ClaimDetailResult.Closed.NotCovered,
    )
)
