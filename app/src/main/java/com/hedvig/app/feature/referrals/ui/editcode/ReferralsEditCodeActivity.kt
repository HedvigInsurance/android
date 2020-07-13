package com.hedvig.app.feature.referrals.ui.editcode

import android.content.Context
import android.content.Intent
import com.hedvig.app.BaseActivity
import com.hedvig.app.R

class ReferralsEditCodeActivity : BaseActivity(R.layout.activity_referrals_edit_code) {
    companion object {
        fun newInstance(context: Context) = Intent(context, ReferralsEditCodeActivity::class.java)
    }
}
