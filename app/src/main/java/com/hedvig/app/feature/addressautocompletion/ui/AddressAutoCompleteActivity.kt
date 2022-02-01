package com.hedvig.app.feature.addressautocompletion.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hedvig.app.feature.addressautocompletion.activityresult.FetchDanishAddressContract
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity
import com.hedvig.app.ui.compose.theme.HedvigTheme

class AddressAutoCompleteActivity : AppCompatActivity() {

    private val viewModel: AddressAutoCompleteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewState by viewModel.viewState.collectAsState()
            HedvigTheme {
                AddressAutoCompleteScreen(
                    viewState = viewState,
                    selectAddress = viewModel::selectAddress,
                    setInput = viewModel::setNewInput,
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
        fun newInstance(context: Context) = Intent(context, RetrievePriceInfoActivity::class.java)
    }
}
