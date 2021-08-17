package com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hedvig.app.feature.embark.passages.previousinsurer.askforprice.RetrievePriceContent
import com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice.RetrievePriceViewModel.ViewState.Loading
import com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice.RetrievePriceViewModel.ViewState.RetrievePrice
import com.hedvig.app.ui.compose.composables.CenteredProgressIndicator
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
                            title = "Retrieve price info"
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
    Crossfade(targetState = viewModel.viewState.collectAsState().value) { viewState ->
        when (viewState) {
            RetrievePrice -> RetrievePriceContent(
                onRetrievePriceInfo = viewModel::onRetrievePriceInfo,
                onIdentityInput = { viewModel.onIdentityInput(it) }
            )
            Loading -> CenteredProgressIndicator()
        }
    }
}
