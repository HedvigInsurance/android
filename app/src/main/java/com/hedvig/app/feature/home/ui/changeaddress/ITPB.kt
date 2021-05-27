package com.hedvig.app.feature.home.ui.changeaddress

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme

@Composable
fun ITPB(
    illustration: Painter,
    title: String,
    paragraph: String,
    onClick: () -> Unit,
    buttonContent: @Composable RowScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    Column {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f, true)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Image(
                painter = illustration,
                contentDescription = null,
                modifier = Modifier.wrapContentSize()
            )
            Text(
                text = title,
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 40.dp),
            )
            Text(
                text = paragraph,
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp)
            )
        }

        Button(
            onClick = onClick,
            contentPadding = PaddingValues(16.dp),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            content = buttonContent,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ITPBPreview() {
    MdcTheme {
        ITPB(
            illustration = painterResource(android.R.drawable.star_big_on),
            title = "Preview Title",
            paragraph = "Example paragraph that takes a up a little bit of space. Lorem ipsum yada yada.",
            onClick = {},
            buttonContent = { Text("Example button text") }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TPBPreviewDark() {
    MdcTheme {
        ITPB(
            illustration = painterResource(android.R.drawable.star_big_on),
            title = "Preview Title",
            paragraph = "Example paragraph that takes a up a little bit of space. Lorem ipsum yada yada.",
            onClick = {},
            buttonContent = { Text("Example button text") }
        )
    }
}
