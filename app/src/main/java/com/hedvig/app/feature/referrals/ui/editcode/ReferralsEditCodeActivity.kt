package com.hedvig.app.feature.referrals.ui.editcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.observe
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import e
import kotlinx.android.synthetic.main.activity_referrals_edit_code.*
import org.koin.android.viewmodel.ext.android.viewModel

class ReferralsEditCodeActivity : BaseActivity(R.layout.activity_referrals_edit_code) {
    private val model: ReferralsEditCodeViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)

        val currentCode = intent.getStringExtra(CODE)

        if (currentCode == null) {
            e { "Programmer error: `CODE` not passed to ${this.javaClass.name}" }
        }

        code.setText(currentCode)

        model.data.observe(this) { data ->
            data.updateReferralCampaignCode.asSuccessfullyUpdatedCode?.let {
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.referrals_edit_code_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.save -> {
            // TODO: Check that everything is valid before making call
            model.changeCode(code.text.toString())
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        private const val CODE = "CODE"
        fun newInstance(context: Context, currentCode: String) =
            Intent(context, ReferralsEditCodeActivity::class.java).apply {
                putExtra(CODE, currentCode)
            }
    }
}
