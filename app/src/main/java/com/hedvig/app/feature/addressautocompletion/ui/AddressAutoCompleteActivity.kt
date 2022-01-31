package com.hedvig.app.feature.addressautocompletion.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity
import com.hedvig.app.ui.compose.theme.HedvigTheme

class AddressAutoCompleteActivity : AppCompatActivity() {

    private val viewModel: AddressAutoCompleteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HedvigTheme {
                AddressAutoCompleteScreen()
            }
        }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, RetrievePriceInfoActivity::class.java)
    }
}
