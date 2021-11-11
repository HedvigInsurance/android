package com.hedvig.app.feature.claimstatus.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.hedvig.app.R
import com.hedvig.app.feature.claimstatus.model.ClaimStatusDetailData
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.compose.preview.previewData

@Composable
fun ClaimType(
    themedIconUrls: ThemedIconUrls,
    claimType: ClaimStatusDetailData.ClaimInfoData.ClaimType,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = rememberImagePainter(
                data = themedIconUrls.iconUrl(),
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(24.dp)
        )
        when (claimType) {
            is ClaimStatusDetailData.ClaimInfoData.ClaimType.Known -> {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = claimType.title,
                        style = MaterialTheme.typography.h6
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = claimType.insuranceType,
                            style = MaterialTheme.typography.subtitle2
                        )
                    }
                }
            }
            ClaimStatusDetailData.ClaimInfoData.ClaimType.Unknown -> {
                Text(
                    text = stringResource(R.string.claim_casetype_insurance_case),
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClaimTypePreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimType(
                themedIconUrls = ThemedIconUrls.previewData(),
                claimType = ClaimStatusDetailData.ClaimInfoData.ClaimType.previewData()
            )
        }
    }
}

@Preview
@Composable
fun ClaimTypePreview2() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimType(
                themedIconUrls = ThemedIconUrls.previewData(),
                claimType = ClaimStatusDetailData.ClaimInfoData.ClaimType.Unknown
            )
        }
    }
}
