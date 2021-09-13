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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.HedvigTheme
import com.hedvig.app.ui.compose.designsystem.LargeContainedButton
import com.hedvig.app.ui.compose.designsystem.LargeOutlinedButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// TODO text resources
@Composable
fun CrossSellingResultScreen(
    crossSellingResult: CrossSellingResult
) {
    HedvigTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            InformationSection(
                crossSellingResult = crossSellingResult,
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
    modifier: Modifier
) {
    @DrawableRes
    val drawableId: Int = when (crossSellingResult) {
        is CrossSellingResult.Success -> R.drawable.ic_checkmark_in_circle
        CrossSellingResult.Error -> R.drawable.ic_x_in_circle
    }
    val titleText = when (crossSellingResult) {
        CrossSellingResult.Error -> "Something went wrong."
        is CrossSellingResult.Success -> "You now have accident insurance."
    }
    val descriptionText = when (crossSellingResult) {
        CrossSellingResult.Error -> "Your purchase couldn't be completed.\nContact us in the chat."
        is CrossSellingResult.Success -> {
            when {
                crossSellingResult.startingDate.dayOfMonth <= LocalDate.now().dayOfMonth -> {
                    "It's already activated"
                }
                else -> {
                    val activationDate = crossSellingResult.startingDate.format(
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    "It'll activate $activationDate"
                }
            }
        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Image(
            painter = painterResource(drawableId),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Text(
            titleText,
            style = MaterialTheme.typography.h5
        )
        Text(
            descriptionText,
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

@Preview(showSystemUi = true, name = "Accident Result")
@Composable
fun CrossSellingResultScreenPreview(
    @PreviewParameter(ActivityResultProvider::class) crossSellingResult: CrossSellingResult,
) {
    HedvigTheme {
        CrossSellingResultScreen(crossSellingResult)
    }
}

class ActivityResultProvider : PreviewParameterProvider<CrossSellingResult> {
    override val values: Sequence<CrossSellingResult> = sequenceOf(
        CrossSellingResult.Error,
        CrossSellingResult.Success(LocalDate.now()),
        CrossSellingResult.Success(LocalDate.now().plusDays(2)),
    )
}
