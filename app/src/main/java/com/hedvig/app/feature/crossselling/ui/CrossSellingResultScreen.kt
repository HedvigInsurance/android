package com.hedvig.app.feature.crossselling.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedTextButton
import com.hedvig.app.ui.compose.composables.buttons.LargeOutlinedTextButton
import com.hedvig.app.ui.compose.theme.HedvigTheme
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CrossSellingResultScreen(
    crossSellingResult: CrossSellingResult,
    clock: Clock,
    dateFormatter: DateTimeFormatter,
    openChat: () -> Unit,
    closeResultScreen: () -> Unit,
) {
    HedvigTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            InformationSection(
                crossSellingResult = crossSellingResult,
                clock = clock,
                dateFormatter = dateFormatter,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 96.dp)
                    .fillMaxWidth(),
            )
            ButtonsSection(
                crossSellingResult = crossSellingResult,
                openChat = openChat,
                closeResultScreen = closeResultScreen,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun InformationSection(
    crossSellingResult: CrossSellingResult,
    clock: Clock,
    dateFormatter: DateTimeFormatter,
    modifier: Modifier
) {
    val icon: Painter = when (crossSellingResult) {
        is CrossSellingResult.Success -> painterResource(R.drawable.ic_checkmark_in_circle)
        CrossSellingResult.Error -> painterResource(R.drawable.ic_x_in_circle)
    }
    val titleText = when (crossSellingResult) {
        CrossSellingResult.Error -> stringResource(R.string.purchase_confirmation_error_title)
        is CrossSellingResult.Success -> {
            stringResource(
                R.string.purchase_confirmation_new_insurance_today_app_state_title,
                crossSellingResult.insuranceType
            )
        }
    }
    val subtitleText = when (crossSellingResult) {
        CrossSellingResult.Error -> stringResource(R.string.purchase_confirmation_error_subtitle)
        is CrossSellingResult.Success -> {
            when {
                crossSellingResult.startingDate <= LocalDate.now(clock) -> {
                    stringResource(
                        R.string.purchase_confirmation_new_insurance_today_app_state_description,
                        crossSellingResult.insuranceType
                    )
                }
                else -> {
                    val activationDate = crossSellingResult.startingDate.format(dateFormatter)
                    stringResource(
                        R.string.purchase_confirmation_new_insurance_active_in_future_app_state_description,
                        crossSellingResult.insuranceType,
                        activationDate
                    )
                }
            }
        }
    }
    InformationSection(icon, titleText, subtitleText, modifier)
}

@Composable
private fun InformationSection(
    icon: Painter,
    title: String,
    description: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            title,
            style = MaterialTheme.typography.h5
        )
        Spacer(Modifier.height(24.dp))
        Text(
            description,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun ButtonsSection(
    crossSellingResult: CrossSellingResult,
    openChat: () -> Unit,
    closeResultScreen: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (crossSellingResult) {
            is CrossSellingResult.Error -> {
                LargeContainedButton(
                    onClick = openChat,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_chat_on_primary),
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.purchase_confirmation_error_open_chat_button))
                }
                LargeOutlinedTextButton(
                    onClick = closeResultScreen,
                    text = stringResource(R.string.purchase_confirmation_error_close_button)
                )
            }
            is CrossSellingResult.Success -> {
                LargeContainedTextButton(
                    onClick = closeResultScreen,
                    text = stringResource(R.string.purchase_confirmation_accident_insurance_done_button)
                )
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    name = "Accident Result",
    group = "Cross Sell result"
)
@Composable
fun CrossSellingResultScreenPreview(
    @PreviewParameter(ActivityResultProvider::class) crossSellingResult: CrossSellingResult,
) {
    HedvigTheme {
        CrossSellingResultScreen(
            crossSellingResult,
            Clock.systemDefaultZone(),
            DateTimeFormatter.ISO_LOCAL_DATE,
            {},
            {}
        )
    }
}

class ActivityResultProvider : PreviewParameterProvider<CrossSellingResult> {
    override val values: Sequence<CrossSellingResult> = sequenceOf(
        CrossSellingResult.Error,
        CrossSellingResult.Success(LocalDate.now(), "Accident Insurance"),
        CrossSellingResult.Success(LocalDate.now().plusDays(2), "Accident Insurance"),
    )
}
