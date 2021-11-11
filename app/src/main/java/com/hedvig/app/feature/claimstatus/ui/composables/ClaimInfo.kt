package com.hedvig.app.feature.claimstatus.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.claimstatus.model.ClaimStatusDetailData
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.compose.preview.previewData
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun ClaimInfo(
    topInfoData: ClaimStatusDetailData.TopInfoData,
    locale: Locale,
) {
    Column {
        ClaimType(
            themedIconUrls = topInfoData.themedIconUrls,
            claimType = topInfoData.claimType,
        )
        Spacer(Modifier.height(16.dp))
        ClaimDates(
            submittedAt = topInfoData.submittedAt,
            closedAt = topInfoData.closedAt,
            locale = locale,
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClaimInfoPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimInfo(
                ClaimStatusDetailData.TopInfoData(
                    themedIconUrls = ThemedIconUrls.previewData(),
                    claimType = ClaimStatusDetailData.TopInfoData.ClaimType.previewData(),
                    submittedAt = Instant.now().minus(10, ChronoUnit.DAYS),
                    closedAt = Instant.now().minus(1, ChronoUnit.DAYS),
                ),
                Locale.forLanguageTag("sv-SE")
            )
        }
    }
}
