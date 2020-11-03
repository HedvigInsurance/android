package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity

class PerilInfoActivity : BaseActivity(R.layout.activity_peril_info) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        fun newInstance(context: Context) =
            Intent(context, ContractDetailActivity::class.java).apply {

            }
    }
}
