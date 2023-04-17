package com.hedvig.app.feature.addressautocompletion.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.addressautocompletion.activityresult.FetchDanishAddressContract
import com.hedvig.app.feature.addressautocompletion.activityresult.FetchDanishAddressContractResult
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class AddressAutoCompleteActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.compatSetDecorFitsSystemWindows(false)

    val initialAddress: DanishAddress? = intent.parcelableExtra(INITIAL_ADDRESS_KEY)
    val viewModel: AddressAutoCompleteViewModel = getViewModel { parametersOf(initialAddress) }

    setContent {
      val viewState by viewModel.viewState.collectAsState()
      LaunchedEffect(viewState.selectedFinalAddress) {
        val selectedFinalAddress = viewState.selectedFinalAddress ?: return@LaunchedEffect
        finishWithResult(FetchDanishAddressContractResult.Selected(selectedFinalAddress))
      }
      HedvigTheme {
        AddressAutoCompleteScreen(
          viewState = viewState,
          setNewTextInput = viewModel::setNewTextInput,
          selectAddress = viewModel::selectAddress,
          cancelAutoCompletion = { finishWithResult(FetchDanishAddressContractResult.Canceled) },
          cantFindAddress = { finishWithResult(FetchDanishAddressContractResult.CantFind) },
        )
      }
    }
  }

  private fun finishWithResult(result: FetchDanishAddressContractResult) {
    setResult(
      FetchDanishAddressContract.RESULT_CODE,
      Intent().putExtra(FetchDanishAddressContract.FETCH_DANISH_ADDRESS_CONTRACT_RESULT_KEY, result),
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
