package com.hedvig.app.feature.insurance.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailActivityBinding
import com.hedvig.app.feature.insurance.ui.ContractModel
import com.hedvig.app.feature.insurance.ui.ContractPillAdapter
import com.hedvig.app.feature.insurance.ui.detail.documents.DocumentsFragment
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoFragment
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import org.koin.android.viewmodel.ext.android.viewModel
import java.time.format.DateTimeFormatter

class ContractDetailActivity : BaseActivity(R.layout.contract_detail_activity) {
    private val binding by viewBinding(ContractDetailActivityBinding::bind)
    private val model: ContractDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(ID)

        if (id == null) {
            e { "Programmer error: ID not provided to ${this.javaClass.name}" }
            return
        }
        model.data.observe(this) { contract ->
            binding.apply {
                contract.status.fragments.contractStatusFragment.let { contractStatus ->
                    contractStatus.asPendingStatus?.let {
                        firstStatusPill.show()
                        firstStatusPill.setText(R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_NO_STARTDATE)
                    }
                    contractStatus.asActiveInFutureStatus?.let { activeInFuture ->
                        firstStatusPill.show()
                        firstStatusPill.text = firstStatusPill.resources.getString(
                            R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE,
                            dateTimeFormatter.format(activeInFuture.futureInception)
                        )
                    }
                    contractStatus.asActiveInFutureAndTerminatedInFutureStatus?.let { activeAndTerminated ->
                        firstStatusPill.show()
                        firstStatusPill.text = firstStatusPill.resources.getString(
                            R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE,
                            dateTimeFormatter.format(activeAndTerminated.futureInception)
                        )
                        secondStatusPill.show()
                        secondStatusPill.text = secondStatusPill.context.getString(
                            R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_TERMINATIONDATE,
                            dateTimeFormatter.format(activeAndTerminated.futureTermination)
                        )
                    }
                    contractStatus.asTerminatedInFutureStatus?.let { terminatedInFuture ->
                        firstStatusPill.show()
                        firstStatusPill.text = firstStatusPill.resources.getString(
                            R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_TERMINATIONDATE,
                            dateTimeFormatter.format(terminatedInFuture.futureTermination)
                        )
                    }
                    contractStatus.asTerminatedTodayStatus?.let {
                        firstStatusPill.show()
                        firstStatusPill.setText(R.string.DASHBOARD_INSURANCE_STATUS_TERMINATED_TODAY)
                    }
                    contractStatus.asTerminatedStatus?.let {
                        firstStatusPill.show()
                        firstStatusPill.setText(R.string.DASHBOARD_INSURANCE_STATUS_TERMINATED)
                    }
                    contractStatus.asActiveStatus?.let {
                        when (contract.typeOfContract) {
                            TypeOfContract.SE_HOUSE,
                            TypeOfContract.SE_APARTMENT_BRF,
                            TypeOfContract.SE_APARTMENT_RENT,
                            TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                            TypeOfContract.SE_APARTMENT_STUDENT_RENT,
                            TypeOfContract.NO_HOME_CONTENT_OWN,
                            TypeOfContract.NO_HOME_CONTENT_RENT,
                            TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                            TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT -> {
                                container.setBackgroundResource(R.drawable.card_home_background)
                            }
                            TypeOfContract.NO_TRAVEL,
                            TypeOfContract.NO_TRAVEL_YOUTH,
                            TypeOfContract.DK_HOME_CONTENT -> {
                                container.setBackgroundResource(R.drawable.card_travel_background)
                            }
                            TypeOfContract.UNKNOWN__ -> {

                            }
                        }
                    } ?: run {
                        container.setBackgroundResource(R.color.hedvig_light_gray)
                    }
                }


                contractName.text = contract.displayName

                contractPills.adapter = ContractPillAdapter().also { adapter ->
                    when (contract.typeOfContract) {
                        TypeOfContract.SE_HOUSE,
                        TypeOfContract.SE_APARTMENT_BRF,
                        TypeOfContract.SE_APARTMENT_RENT,
                        TypeOfContract.NO_HOME_CONTENT_OWN,
                        TypeOfContract.NO_HOME_CONTENT_RENT,
                        TypeOfContract.DK_HOME_CONTENT,
                        TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                        TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                        TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                        TypeOfContract.SE_APARTMENT_STUDENT_RENT -> {
                            adapter.submitList(
                                listOf(
                                    ContractModel.Address(contract.currentAgreement),
                                    ContractModel.NoOfCoInsured(contract.currentAgreement.numberCoInsured)
                                )
                            )
                        }
                        TypeOfContract.NO_TRAVEL,
                        TypeOfContract.NO_TRAVEL_YOUTH -> {
                            adapter.submitList(listOf(ContractModel.NoOfCoInsured(contract.currentAgreement.numberCoInsured)))
                        }
                        TypeOfContract.UNKNOWN__ -> {
                        }
                    }
                }
            }
        }
        model.loadContract(id)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.apply {
                doOnApplyWindowInsets { view, insets, initialState ->
                    view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
                }
                setNavigationOnClickListener {
                    onBackPressed()
                }
            }
            arrow.remove()
            tabContent.adapter = ContractDetailTabAdapter(this@ContractDetailActivity)
            TabLayoutMediator(tabContainer, tabContent) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = "Your info"
                    }
                    1 -> {
                        tab.text = "Coverage"
                    }
                    2 -> {
                        tab.text = "Documents"
                    }
                    else -> {
                        e { "Invalid tab index: $position" }
                    }
                }
            }.attach()
        }
    }

    companion object {
        private const val ID = "ID"
        fun newInstance(context: Context, id: String) =
            Intent(context, ContractDetailActivity::class.java).apply {
                putExtra(ID, id)
            }

        private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu")

        private val InsuranceQuery.CurrentAgreement.numberCoInsured: Int
            get() {
                asNorwegianTravelAgreement?.numberCoInsured?.let { return it }
                asSwedishHouseAgreement?.numberCoInsured?.let { return it }
                asSwedishApartmentAgreement?.numberCoInsured?.let { return it }
                asNorwegianHomeContentAgreement?.numberCoInsured?.let { return it }
                e { "Unable to infer amount coinsured for agreement: $this" }
                return 0
            }
    }
}

class ContractDetailTabAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 3
    override fun createFragment(position: Int) = when (position) {
        0 -> YourInfoFragment()
        1 -> Fragment()
        2 -> DocumentsFragment()
        else -> Fragment()
    }
}
