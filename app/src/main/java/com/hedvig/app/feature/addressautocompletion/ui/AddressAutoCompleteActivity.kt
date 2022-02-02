package com.hedvig.app.feature.addressautocompletion.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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

        val initialText = intent.getStringExtra(INITIAL_TEXT_KEY)
            ?: throw IllegalArgumentException("Programmer error: Missing initial text in ${this.javaClass.name}")
        val viewModel: AddressAutoCompleteViewModel by viewModel { parametersOf(initialText) }

        setContent {
            val viewState by viewModel.viewState.collectAsState()
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
            Intent().putExtra(FetchDanishAddressContract.ADDRESS_STRING_KEY, address.toFlatQueryString())
        )
        finish()
    }

    companion object {
        const val INITIAL_TEXT_KEY = "com.hedvig.app.feature.addressautocompletion.ui.INITIAL_TEXT_KEY"

        fun newInstance(
            context: Context,
            initialText: String?,
        ) = Intent(context, AddressAutoCompleteActivity::class.java)
            .putExtra(INITIAL_TEXT_KEY, initialText)
    }
}
