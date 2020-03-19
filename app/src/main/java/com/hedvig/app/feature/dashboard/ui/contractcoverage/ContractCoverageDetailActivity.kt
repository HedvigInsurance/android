package com.hedvig.app.feature.dashboard.ui.contractcoverage

import android.os.Bundle
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.ui.Contract
import com.hedvig.app.feature.dashboard.ui.PerilsAdapter
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.extensions.observe
import kotlinx.android.synthetic.main.activity_contract_coverage_detail.*
import org.koin.android.viewmodel.ext.android.viewModel

class ContractCoverageDetailActivity : BaseActivity(R.layout.activity_contract_coverage_detail) {
    private val model: ContractCoverageDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        perils.adapter = PerilsAdapter(supportFragmentManager)
        perils.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))

        model.data.observe(this) { data ->
            data?.let { bind(it) }
        }
    }

    private fun bind(data: Contract) {
        (perils.adapter as? PerilsAdapter)?.setData("TODO", data.perils)
    }
}
