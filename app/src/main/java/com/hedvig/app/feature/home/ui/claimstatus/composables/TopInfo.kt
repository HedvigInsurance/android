package com.hedvig.app.feature.home.ui.claimstatus.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.DarkAndLightColor
import com.hedvig.app.util.compose.DisplayableText

@Composable
fun TopInfo(
    pillDataList: List<PillData>,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Pills(pillDataList)
        Spacer(modifier = Modifier.height(20.dp))
        Text(title)
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.body2
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TopInfoPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            TopInfo(
                pillDataList = listOf(
                    PillData(
                        DisplayableText("Reopened"),
                        PillData.PillType.Contained(DarkAndLightColor(Color(0xFFFE9650)))
                    ),
                    PillData(DisplayableText("Claim"), PillData.PillType.Outlined),
                ),
                title = "All-risk",
                subtitle = "Contents insurance",
            )
        }
    }
}
