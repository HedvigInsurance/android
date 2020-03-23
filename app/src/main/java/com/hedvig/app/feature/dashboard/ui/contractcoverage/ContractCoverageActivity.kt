package com.hedvig.app.feature.dashboard.ui.contractcoverage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.svg.buildRequestBuilder
import e
import kotlinx.android.synthetic.main.activity_contract_coverage_detail.*
import org.koin.android.viewmodel.ext.android.viewModel

class ContractCoverageActivity : BaseActivity(R.layout.activity_contract_coverage_detail) {
    private val model: ContractCoverageViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        perils.adapter = PerilsAdapter(supportFragmentManager, buildRequestBuilder())
        perils.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val id = intent.getStringExtra(ID)
        if (id == null) {
            e { "Programmer error: ID not provided to ${this.javaClass.name}" }
            return
        }

        model.data.observe(this) { data ->
            data?.let { bind(it) }
        }
        model.loadContract(id)
    }

    private fun bind(data: DashboardQuery.Contract) {
        loadingSpinner.remove()
        scrollView.show()
        (perils.adapter as? PerilsAdapter)?.items = data.perils
    }

    companion object {
        private const val ID = "ID"
        fun newInstance(context: Context, id: String) = Intent(context, ContractCoverageActivity::class.java).apply {
            putExtra(ID, id)
        }
    }
}
