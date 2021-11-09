package com.hedvig.app.feature.claimstatus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.hedvig.app.BaseActivity
import com.hedvig.app.feature.claimstatus.ui.ClaimStatusDetailScreen
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import e

class ClaimStatusDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.compatSetDecorFitsSystemWindows(false)

        val claimId = intent.getStringExtra(CLAIM_ID)
        requireNotNull(claimId) {
            e { "Programmer error: CLAIM_ID not provided to ${this.javaClass.name}" }
            finish()
            return
        }

        setContent {
            HedvigTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ClaimStatusDetailScreen(claimId)
                }
            }
        }
    }

    companion object {
        const val CLAIM_ID = "com.hedvig.app.feature.claimstatus.ClaimStatusActivity:CLAIM_ID"

        fun newInstance(context: Context, claimId: String): Intent =
            Intent(context, ClaimStatusDetailActivity::class.java).apply {
                putExtra(CLAIM_ID, claimId)
            }
    }
}
