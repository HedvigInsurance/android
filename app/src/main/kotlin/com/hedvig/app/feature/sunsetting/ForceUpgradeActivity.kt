package com.hedvig.app.feature.sunsetting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.ratings.tryOpenPlayStore

class ForceUpgradeActivity : AppCompatActivity() {
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
      text = stringResource(hedvig.resources.R.string.EMBARK_UPDATE_APP_TITLE),
      style = MaterialTheme.typography.h4,
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.EMBARK_UPDATE_APP_BODY),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.body1,
    )
    Spacer(Modifier.height(16.dp))
    Button(
      onClick = goToPlayStore,
    ) {
      Text(
        text = stringResource(hedvig.resources.R.string.EMBARK_UPDATE_APP_BUTTON),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewUpgradeApp() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      UpgradeApp(goToPlayStore = {})
    }
  }
}
