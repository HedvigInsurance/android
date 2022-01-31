package com.hedvig.app.feature.addressautocompletion.activityresult

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.hedvig.app.feature.addressautocompletion.ui.AddressAutoCompleteActivity

class FetchDanishAddressContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return AddressAutoCompleteActivity.createIntent(context)
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
