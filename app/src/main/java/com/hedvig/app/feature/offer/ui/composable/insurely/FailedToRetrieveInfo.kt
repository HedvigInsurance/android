package com.hedvig.app.feature.offer.ui.composable.insurely

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun FailedToRetrieveInfo(insuranceProvider: String?) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(
            start = 16.dp,
            top = 20.dp,
            end = 16.dp,
            bottom = 16.dp,
        )
    ) {
        Icon(painterResource(R.drawable.ic_warning_triangle), null)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (insuranceProvider != null) {
                    "Couldn't retrieve info from $insuranceProvider"
                } else {
                    "Couldn't retrieve price info"
                },
                style = MaterialTheme.typography.subtitle1,
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = if (insuranceProvider != null) {
                        "We were unable to retrieve your price info from $insuranceProvider. Sorry about that."
                    } else {
                        "We were unable to retrieve your price info from your current insurers. Sorry about that."
                    },
                    style = MaterialTheme.typography.body2,
                )
            }
        }
    }
}

@Preview
@Composable
fun FailedToRetrieveInfoPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            FailedToRetrieveInfo(null)
        }
    }
}
