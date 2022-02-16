package com.hedvig.app.feature.offer.ui.composable.insurely

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme
import java.util.Locale

@Composable
fun LoadingRetrieval(locale: Locale) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        val resources = LocalContext.current.resources
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = resources.getQuantityString(R.plurals.offer_switcher_title, 2).uppercase(locale),
                style = MaterialTheme.typography.caption,
            )
        }
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = stringResource(R.string.offer_screen_insurely_card_loading_support_text),
                style = MaterialTheme.typography.body2,
            )
        }
    }
}

@Preview
@Composable
fun LoadingRetrievalPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            LoadingRetrieval(Locale.ENGLISH)
        }
    }
}
