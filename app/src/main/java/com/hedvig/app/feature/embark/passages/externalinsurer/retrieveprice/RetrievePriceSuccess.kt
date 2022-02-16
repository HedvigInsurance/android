package com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedTextButton

@Composable
fun RetrievePriceSuccess(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.base_margin_double))
            .fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Image(
            painter = painterResource(R.drawable.ic_checkmark_in_circle),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.insurely_confirmation_title),
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.insurely_confirmation_description)
        )
        Spacer(modifier = Modifier.height(40.dp))
        LargeContainedTextButton(text = stringResource(R.string.continue_button), onClick = onContinue)
    }
}

@Preview
@Composable
fun RetrievePriceSuccessPreview() {
    RetrievePriceSuccess {
    }
}
