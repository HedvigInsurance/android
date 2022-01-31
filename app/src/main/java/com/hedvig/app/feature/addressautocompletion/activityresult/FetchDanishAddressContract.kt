package com.hedvig.app.feature.addressautocompletion.activityresult

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.hedvig.app.feature.addressautocompletion.ui.AddressAutoCompleteActivity
import com.hedvig.app.feature.embark.passages.addressautocomplete.EmbarkAddressAutoCompleteFragment

class FetchDanishAddressContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return AddressAutoCompleteActivity.createIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != EmbarkAddressAutoCompleteFragment.AUTO_COMPLETE_RESULT_CODE) return null
        return intent?.extras?.getString(EmbarkAddressAutoCompleteFragment.AUTO_COMPLETE_RESULT_STRING_KEY)
    }
}
