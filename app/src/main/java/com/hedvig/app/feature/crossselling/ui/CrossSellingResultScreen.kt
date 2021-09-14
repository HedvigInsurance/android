package com.hedvig.app.feature.crossselling.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.HedvigTheme
import com.hedvig.app.ui.compose.designsystem.LargeContainedButton
import com.hedvig.app.ui.compose.designsystem.LargeOutlinedButton
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// TODO text resources still missing
@Composable
fun CrossSellingResultScreen(
    crossSellingResult: CrossSellingResult,
    clock: Clock,
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
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 96.dp)
                    .fillMaxWidth(),
            )
            ButtonsSection(
                crossSellingResult = crossSellingResult,
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
    modifier: Modifier
) {
    @DrawableRes
    val drawableId: Int = when (crossSellingResult) {
        is CrossSellingResult.Success -> R.drawable.ic_checkmark_in_circle
        CrossSellingResult.Error -> R.drawable.ic_x_in_circle
    }
    val titleText = when (crossSellingResult) {
        CrossSellingResult.Error -> "Something went wrong."
        is CrossSellingResult.Success -> {
            stringResource(
                R.string.purchase_confirmation_new_insurance_today_app_state_title,
                crossSellingResult.insuranceType
            )
        }
    }
    val descriptionText = when (crossSellingResult) {
        CrossSellingResult.Error -> "Your purchase couldn't be completed.\nContact us in the chat."
        is CrossSellingResult.Success -> {
            when {
                crossSellingResult.startingDate.dayOfMonth <= LocalDate.now(clock).dayOfMonth -> {
                    stringResource(R.string.purchase_confirmation_new_insurance_today_app_state_description)
                }
                else -> {
                    val activationDate = crossSellingResult.startingDate.format(
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    stringResource(
                        R.string.purchase_confirmation_new_insurance_active_in_future_app_state_description,
                        activationDate
                    )
                }
            }
        }
    }
    InformationSection(drawableId, titleText, descriptionText, modifier)
}

@Composable
private fun InformationSection(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Text(
            title,
            style = MaterialTheme.typography.h5
        )
        Text(
            description,
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun ButtonsSection(
    crossSellingResult: CrossSellingResult,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (crossSellingResult) {
            is CrossSellingResult.Error -> {
                LargeContainedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_chat_white),
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Open chat")
                }
                LargeOutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
            is CrossSellingResult.Success -> {
                LargeContainedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
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
        CrossSellingResultScreen(crossSellingResult, Clock.systemDefaultZone())
    }
}

class ActivityResultProvider : PreviewParameterProvider<CrossSellingResult> {
    override val values: Sequence<CrossSellingResult> = sequenceOf(
        CrossSellingResult.Error,
        CrossSellingResult.Success(LocalDate.now(), "Accident Insurance"),
        CrossSellingResult.Success(LocalDate.now().plusDays(2), "Accident Insurance"),
    )
}
