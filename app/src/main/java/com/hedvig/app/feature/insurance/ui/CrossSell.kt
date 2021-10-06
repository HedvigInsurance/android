package com.hedvig.app.feature.insurance.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.commit451.coiltransformations.CropTransformation
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.hedvigBlack
import com.hedvig.app.ui.compose.theme.hedvigBlack12percent
import com.hedvig.app.ui.compose.theme.whiteHighEmphasis
import com.hedvig.app.util.compose.rememberBlurHash
import com.hedvig.app.util.extensions.makeToast

/*
 * Note: This Composable uses hardcoded colors due to difficulties with
 * declaring a particular component to be in dark theme instead of the
 * default. When we update `HedvigTheme` to be Compose-first instead of
 * XML-Theme first, we can reconfigure the theme for this composable to
 * be `dark` no matter what the system value is.
 */
@Composable
fun CrossSell(
    data: InsuranceModel.CrossSell,
    onCtaClick: () -> Unit,
) {
    val placeholder by rememberBlurHash(data.backgroundBlurHash, 64, 32)
    Card(
        border = BorderStroke(1.dp, hedvigBlack12percent),
        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            )
            .height(200.dp),
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
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = onCtaClick
                ),
        )
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x00000000),
                            Color(0xFF000000),
                        ),
                    )
                )
                .padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(bottom = 4.dp)
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.subtitle1,
                        color = whiteHighEmphasis,
                    )
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.subtitle2,
                        color = whiteHighEmphasis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                CompositionLocalProvider(
                    LocalRippleTheme provides DarkRippleTheme,
                ) {
                    Button(
                        onClick = onCtaClick,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = whiteHighEmphasis,
                            contentColor = hedvigBlack,
                        ),
                    ) {
                        Text(
                            text = data.callToAction,
                        )
                    }
                }
            }
        }
    }
}

private object DarkRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color(0x1f000000)

    @Composable
    override fun rippleAlpha() = RippleTheme.defaultRippleAlpha(
        contentColor = LocalContentColor.current,
        lightTheme = false,
    )
}

private val previewData = InsuranceModel.CrossSell(
    title = "Accident Insurance",
    description = "179 kr/mo.",
    callToAction = "Calculate price",
    action = InsuranceModel.CrossSell.Action.Chat,
    backgroundUrl = "https://images.unsplash.com/photo-1628996796855-0b056a464e06",
    backgroundBlurHash = "LJC6\$2-:DiWB~WxuRkayMwNGo~of",
)

@Preview(
    name = "Cross-Sell Card",
    group = "Insurance Tab",
    showBackground = true,
)
@Composable
fun CrossSellPreview() {
    val context = LocalContext.current
    HedvigTheme {
        CrossSell(
            data = previewData,
            onCtaClick = { context.makeToast("Doing stuff") }
        )
    }
}
