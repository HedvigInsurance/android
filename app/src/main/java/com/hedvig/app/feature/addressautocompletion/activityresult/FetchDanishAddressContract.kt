package com.hedvig.app.feature.addressautocompletion.activityresult

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.hedvig.app.feature.addressautocompletion.ui.AddressAutoCompleteActivity

/**
 * Input: String?   The initial text that will be used as the initial text for autocompletion.
 * Output: String?  The autocomplete address result or null.
 */
class FetchDanishAddressContract : ActivityResultContract<String?, String?>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return AddressAutoCompleteActivity.newInstance(context, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != RESULT_CODE) return null
        return intent?.extras?.getString(ADDRESS_STRING_KEY)
    }

    companion object {
        const val RESULT_CODE = 1
        const val ADDRESS_STRING_KEY = "com.hedvig.app.feature.addressautocompletion.activityresult.ADDRESS_STRING_KEY"
    }
}
