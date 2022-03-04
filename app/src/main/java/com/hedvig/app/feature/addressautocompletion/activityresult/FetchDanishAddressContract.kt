package com.hedvig.app.feature.addressautocompletion.activityresult

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.ui.AddressAutoCompleteActivity
import kotlinx.parcelize.Parcelize

/**
 * Input: DanishAddress? The initial address that will be used as the initial text for autocompletion.
 * Output: FetchDanishAddressContractResult The result potentially containing the autocomplete address result.
 */
class FetchDanishAddressContract : ActivityResultContract<DanishAddress?, FetchDanishAddressContractResult>() {
    override fun createIntent(context: Context, input: DanishAddress?): Intent {
        return AddressAutoCompleteActivity.newInstance(context, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): FetchDanishAddressContractResult {
        if (resultCode != RESULT_CODE) return FetchDanishAddressContractResult.Canceled
        return intent?.extras
            ?.getParcelable(FETCH_DANISH_ADDRESS_CONTRACT_RESULT_KEY)
            ?: FetchDanishAddressContractResult.Canceled
    }

    companion object {
        const val RESULT_CODE = 1
        const val FETCH_DANISH_ADDRESS_CONTRACT_RESULT_KEY =
            "com.hedvig.app.feature.addressautocompletion.activityresult.FETCH_DANISH_ADDRESS_CONTRACT_RESULT_KEY"
    }
}

sealed interface FetchDanishAddressContractResult : Parcelable {
    @Parcelize
    object Canceled : FetchDanishAddressContractResult

    @Parcelize
    object CantFind : FetchDanishAddressContractResult

    @Parcelize
    data class Selected(val address: DanishAddress) : FetchDanishAddressContractResult
}
