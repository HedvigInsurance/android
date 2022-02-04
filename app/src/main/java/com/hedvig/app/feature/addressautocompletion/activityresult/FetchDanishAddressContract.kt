package com.hedvig.app.feature.addressautocompletion.activityresult

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.ui.AddressAutoCompleteActivity

/**
 * Input: DanishAddress?   The initial address that will be used as the initial text for autocompletion.
 * Output: DanishAddress?  The autocomplete address result or null.
 */
class FetchDanishAddressContract : ActivityResultContract<DanishAddress?, DanishAddress?>() {
    override fun createIntent(context: Context, input: DanishAddress?): Intent {
        return AddressAutoCompleteActivity.newInstance(context, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): DanishAddress? {
        if (resultCode != RESULT_CODE) return null
        return intent?.extras?.getParcelable(ADDRESS_KEY)
    }

    companion object {
        const val RESULT_CODE = 1
        const val ADDRESS_KEY = "com.hedvig.app.feature.addressautocompletion.activityresult.ADDRESS_KEY"
    }
}
