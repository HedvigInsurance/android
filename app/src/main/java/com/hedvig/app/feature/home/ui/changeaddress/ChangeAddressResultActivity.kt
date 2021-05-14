package com.hedvig.app.feature.home.ui.changeaddress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.hedvig.app.BaseActivity
import com.hedvig.app.R

class ChangeAddressResultActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChangeAddressScreen() }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, ChangeAddressResultActivity::class.java)
    }
}

@Composable
fun ChangeAddressScreen() {
    MdcTheme {
        val scrollState = rememberScrollState()
        Surface(color = MaterialTheme.colors.background) {
            Column {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f, true)
                        .verticalScroll(scrollState)
                ) {
                    Image(
                        painter = painterResource(R.drawable.illustration_claims),
                        contentDescription = null,
                        modifier = Modifier.wrapContentSize()
                    )
                    Text(
                        text = "Congrats to your new home!",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier
                            .padding(top = 40.dp),
                    )
                    Text(
                        text = "Your insurance will be automatically updated to your new address automatically on 23-02-2021",
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 16.dp)
                    )
                }

                Button(
                    onClick = { /* TODO */ },
                    contentPadding = PaddingValues(16.dp),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "OK, close",
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChangeAddressPreview() {
    ChangeAddressScreen()
}
