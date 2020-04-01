package com.hedvig.app.feature.dashboard.ui.contractcoverage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
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

        insurableLimits.adapter = InsurableLimitsAdapter()
        insurableLimits.addItemDecoration((GridSpacingItemDecoration(BASE_MARGIN_DOUBLE)))

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
        perilSectionTitle.text = interpolateTextKey(
            getString(R.string.CONTRACT_COVERAGE_CONTRACT_TYPE),
            "CONTRACT_TYPE" to data.typeOfContract.displayNameDefinite(this)
        )
        (perils.adapter as? PerilsAdapter)?.items = data.perils
        (insurableLimits.adapter as? InsurableLimitsAdapter)?.items = data.insurableLimits
    }

    companion object {
        private const val ID = "ID"

        private fun TypeOfContract.displayNameDefinite(context: Context) = when (this) {
            TypeOfContract.SE_HOUSE,
            TypeOfContract.SE_APARTMENT_BRF,
            TypeOfContract.SE_APARTMENT_RENT,
            TypeOfContract.SE_APARTMENT_STUDENT_BRF,
            TypeOfContract.SE_APARTMENT_STUDENT_RENT,
            TypeOfContract.NO_HOME_CONTENT_OWN,
            TypeOfContract.NO_HOME_CONTENT_RENT,
            TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
            TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT -> context.getString(R.string.INSURANCE_TYPE_HOME_DEFINITE)
            TypeOfContract.NO_TRAVEL,
            TypeOfContract.NO_TRAVEL_YOUTH -> context.getString(R.string.INSURANCE_TYPE_TRAVEL_DEFINITE)
            TypeOfContract.UNKNOWN__ -> ""
        }

        fun newInstance(context: Context, id: String) = Intent(context, ContractCoverageActivity::class.java).apply {
            putExtra(ID, id)
        }
    }
}
