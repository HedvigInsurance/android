package com.hedvig.app.feature.insurance.ui.contractcoverage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.widget.NestedScrollView
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.svg.buildRequestBuilder
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import kotlinx.android.synthetic.main.activity_contract_coverage_detail.*
import org.koin.android.viewmodel.ext.android.viewModel

class ContractCoverageActivity : BaseActivity(R.layout.activity_contract_coverage_detail) {
    private val model: ContractCoverageViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.setEdgeToEdgeSystemUiFlags(true)

        scrollView.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                top = initialState.paddings.top + insets.systemWindowInsetTop,
                bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
            )
        }
        hedvigToolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }

        perils.adapter = PerilsAdapter(supportFragmentManager, buildRequestBuilder())
        perils.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))

        insurableLimits.adapter = InsurableLimitsAdapter()
        insurableLimits.addItemDecoration((GridSpacingItemDecoration(BASE_MARGIN_DOUBLE)))

        hedvigToolbar.setNavigationOnClickListener {
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
        setupScrollListener()
    }

    private fun setupScrollListener() {
        scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            val dy = oldScrollY - scrollY
            hedvigToolbar?.let { toolbar ->
                val toolbarHeight = toolbar.height.toFloat()
                val offset = scrollView.computeVerticalScrollOffset().toFloat()
                val percentage = if (offset < toolbarHeight) {
                    offset / toolbarHeight
                } else {
                    1f
                }
                if (dy < 0) {
                    // Scroll up
                    toolbar.elevation = percentage * 10
                } else {
                    // scroll down
                    toolbar.elevation = percentage * 10
                }
            }
        }
    }

    private fun bind(data: InsuranceQuery.Contract) {
        loadingSpinner.remove()
        scrollView.show()
        perilSectionTitle.text = getString(
            R.string.CONTRACT_COVERAGE_CONTRACT_TYPE,
            data.typeOfContract.displayNameDefinite(this)
        )
        (perils.adapter as? PerilsAdapter)?.items = data.perils.map { it.fragments.perilFragment }
        (insurableLimits.adapter as? InsurableLimitsAdapter)?.items =
            data.insurableLimits.map { it.fragments.insurableLimitsFragment }
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

        fun newInstance(context: Context, id: String) =
            Intent(context, ContractCoverageActivity::class.java).apply {
                putExtra(ID, id)
            }
    }
}
