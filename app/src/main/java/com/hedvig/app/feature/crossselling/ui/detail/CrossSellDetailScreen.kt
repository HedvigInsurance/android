package com.hedvig.app.feature.crossselling.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.commit451.coiltransformations.CropTransformation
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.rememberBlurHash

@Composable
fun CrossSellDetailScreen(
    data: InsuranceModel.CrossSell, // TODO: Replace with a common one instead
    onCtaClick: () -> Unit,
) {
    val placeholder by rememberBlurHash(
        data.backgroundBlurHash,
        64,
        32,
    )
    Surface(
        color = MaterialTheme.colors.background,
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = data.backgroundUrl,
                        builder = {
                            transformations(CropTransformation())
                            placeholder(placeholder)
                            crossfade(true)
                        },
                    ),
                    contentDescription = null,
                    modifier = Modifier.height(260.dp),
                )
            }
            Text(
                text = data.title,
                style = MaterialTheme.typography.h5,
            )
            Text(
                text = data.description,
                style = MaterialTheme.typography.body2,
            )
            LargeContainedButton(onClick = onCtaClick) {
                Text(text="Calculate your price")
            }
        }
    }
}

@Preview
@Composable
fun CrossSellDetailScreenPreview() {
    HedvigTheme {
        CrossSellDetailScreen(
            data = InsuranceModel.CrossSell(
                title = "Accident Insurance",
                description = "179 kr/mo.",
                callToAction = "Calculate price",
                typeOfContract = "SE_ACCIDENT",
                action = InsuranceModel.CrossSell.Action.Chat,
                backgroundUrl = "https://images.unsplash.com/photo-1628996796855-0b056a464e06",
                backgroundBlurHash = "LJC6\$2-:DiWB~WxuRkayMwNGo~of",
            ),
            onCtaClick = {}
        )
    }
}
