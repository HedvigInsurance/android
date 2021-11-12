package com.hedvig.app.feature.claimstatus.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil.ImageLoader
import coil.compose.LocalImageLoader
import com.hedvig.app.BaseActivity
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import e
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale

class ClaimStatusDetailActivity : BaseActivity() {

    val imageLoader by inject<ImageLoader>()
    val localeManager by inject<LocaleManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.compatSetDecorFitsSystemWindows(false)

        val claimId = intent.getStringExtra(CLAIM_ID)
        requireNotNull(claimId) {
            e { "Programmer error: CLAIM_ID not provided to ${this.javaClass.name}" }
            finish()
            return
        }

        val viewModel = getViewModel<ClaimStatusDetailViewModel> {
            parametersOf(claimId)
        }

        setContent {
            // TODO provide proper ImageLoader in HedvigTheme without breaking all Previews due to uninitialized Koin
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                HedvigTheme {
                    val viewState by viewModel.viewState.collectAsState()
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        ClaimStatusDetailScreen(
                            viewState = viewState,
                            locale = Locale.forLanguageTag(localeManager.getLanguageTag()),
                            onBack = { finish() },
                            openChat = { },
                        )
                    }
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
