package com.hedvig.app.feature.home.ui.connectpayincard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.hedvigContentColorFor
import com.hedvig.app.ui.compose.theme.warning

@Composable
fun ConnectPayinCard(
    onActionClick: () -> Unit,
    onShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(key1 = Unit) {
        onShown()
    }

    val colorWarning = MaterialTheme.colors.warning
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        backgroundColor = colorWarning,
        contentColor = hedvigContentColorFor(colorWarning),
    ) {
        Column {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_warning_triangle),
                    contentDescription = null,
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.info_card_missing_payment_title),
                        style = MaterialTheme.typography.subtitle1,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.info_card_missing_payment_body),
                        style = MaterialTheme.typography.body2,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Divider()
            TextButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .padding(
                        top = 4.dp,
                        end = 8.dp,
                        bottom = 4.dp,
                    )
                    .align(Alignment.End),
            ) {
                Text(
                    text = stringResource(R.string.info_card_missing_payment_button_text)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectPayinCardPreview() {
    HedvigTheme {
        ConnectPayinCard(
            onShown = {},
            onActionClick = {},
        )
    }
}
