package com.hedvig.app.feature.referrals.ui.editcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import e
import kotlinx.android.synthetic.main.activity_referrals_edit_code.*

class ReferralsEditCodeActivity : BaseActivity(R.layout.activity_referrals_edit_code) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentCode = intent.getStringExtra(CODE)

        if (currentCode == null) {
            e { "Programmer error: `CODE` not passed to ${this.javaClass.name}" }
        }

        code.setText(currentCode)
    }

    companion object {
        private const val CODE = "CODE"
        fun newInstance(context: Context, currentCode: String) =
            Intent(context, ReferralsEditCodeActivity::class.java).apply {
                putExtra(CODE, currentCode)
            }
    }
}
