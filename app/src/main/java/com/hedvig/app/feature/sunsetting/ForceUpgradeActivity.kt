package com.hedvig.app.feature.sunsetting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.ratings.tryOpenPlayStore
import com.hedvig.app.ui.compose.theme.HedvigTheme

class ForceUpgradeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HedvigTheme {
                UpgradeApp(goToPlayStore = { tryOpenPlayStore() })
            }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, ForceUpgradeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}

@Composable
fun UpgradeApp(
    goToPlayStore: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.EMBARK_UPDATE_APP_TITLE),
            style = MaterialTheme.typography.h4,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.EMBARK_UPDATE_APP_BODY),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = goToPlayStore,
        ) {
            Text(
                text = stringResource(R.string.EMBARK_UPDATE_APP_BUTTON),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpgradeAppPreview() {
    HedvigTheme {
        UpgradeApp(goToPlayStore = {})
    }
}
