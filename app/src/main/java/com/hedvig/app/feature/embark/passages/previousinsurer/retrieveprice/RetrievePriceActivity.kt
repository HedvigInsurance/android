package com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.CenteredProgressIndicator
import com.hedvig.app.ui.compose.composables.FadeWhen
import com.hedvig.app.ui.compose.composables.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class RetrievePriceInfoActivity : ComponentActivity() {

    private val viewModel: RetrievePriceViewModel by viewModel()

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HedvigTheme {
                Scaffold(
                    topBar = {
                        TopAppBarWithBack(
                            onClick = { onBackPressed() },
                            title = stringResource(R.string.insurely_title)
                        )
                    }
                ) {
                    RetrievePriceScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, RetrievePriceInfoActivity::class.java)
    }
}

@ExperimentalAnimationApi
@Composable
fun RetrievePriceScreen(
    viewModel: RetrievePriceViewModel = viewModel()
) {
    val viewState by viewModel.viewState.collectAsState()

    FadeWhen(visible = viewState.isLoading) {
        CenteredProgressIndicator()
    }

    FadeWhen(visible = !viewState.isLoading) {
        RetrievePriceContent(
            onRetrievePriceInfo = viewModel::onRetrievePriceInfo,
            onIdentityInput = { viewModel.onIdentityInput(it) },
            viewState = viewState
        )
    }
}
