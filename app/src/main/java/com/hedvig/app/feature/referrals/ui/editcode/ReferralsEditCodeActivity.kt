package com.hedvig.app.feature.referrals.ui.editcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import e
import kotlinx.android.synthetic.main.activity_referrals_edit_code.*

class ReferralsEditCodeActivity : BaseActivity(R.layout.activity_referrals_edit_code) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)

        val currentCode = intent.getStringExtra(CODE)

        if (currentCode == null) {
            e { "Programmer error: `CODE` not passed to ${this.javaClass.name}" }
        }

        code.setText(currentCode)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.referrals_edit_code_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        private const val CODE = "CODE"
        fun newInstance(context: Context, currentCode: String) =
            Intent(context, ReferralsEditCodeActivity::class.java).apply {
                putExtra(CODE, currentCode)
            }
    }
}
