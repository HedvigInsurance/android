package com.hedvig.app.feature.claimstatus.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.hedvig.app.util.apollo.ThemedIconUrls

@Composable
fun ClaimType(themedIconUrls: ThemedIconUrls, claimType: String, insuranceType: String) {
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
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = claimType,
                style = MaterialTheme.typography.h6
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = insuranceType,
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }
    }
}
