package com.hedvig.app.feature.addressautocompletion.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hedvig.app.feature.addressautocompletion.activityresult.FetchDanishAddressContract
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AddressAutoCompleteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.compatSetDecorFitsSystemWindows(false)

        val initialAddress: DanishAddress? = intent.getParcelableExtra(INITIAL_ADDRESS_KEY)
        val viewModel: AddressAutoCompleteViewModel by viewModel { parametersOf(initialAddress) }

        setContent {
            val viewState by viewModel.viewState.collectAsState()
            LaunchedEffect(viewState.finalResult) {
                val finalResult = viewState.finalResult ?: return@LaunchedEffect
                finishWithAddressResult(finalResult)
            }
            HedvigTheme {
                AddressAutoCompleteScreen(
                    viewState = viewState,
                    setInput = viewModel::setNewInput,
                    selectAddress = viewModel::selectAddress,
                    finishWithSelection = ::finishWithAddressResult,
                    finishWithoutSelection = { finish() },
                )
            }
        }
    }

    private fun finishWithAddressResult(address: DanishAddress) {
        setResult(
            FetchDanishAddressContract.RESULT_CODE,
            Intent().putExtra(FetchDanishAddressContract.ADDRESS_KEY, address)
        )
        finish()
    }

    companion object {
        const val INITIAL_ADDRESS_KEY = "com.hedvig.app.feature.addressautocompletion.ui.INITIAL_ADDRESS_KEY"

        fun newInstance(
            context: Context,
            initialAddress: DanishAddress?,
        ) = Intent(context, AddressAutoCompleteActivity::class.java)
            .putExtra(INITIAL_ADDRESS_KEY, initialAddress)
    }
}
