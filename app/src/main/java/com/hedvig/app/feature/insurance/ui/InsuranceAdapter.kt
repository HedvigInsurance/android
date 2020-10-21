package com.hedvig.app.feature.insurance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.databinding.DashboardUpsellBinding
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.databinding.InsuranceErrorBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import e
import java.time.format.DateTimeFormatter

class InsuranceAdapter(
    private val tracker: InsuranceTracker,
    private val retry: () -> Unit
) :
    RecyclerView.Adapter<InsuranceAdapter.ViewHolder>() {
    var items: List<InsuranceModel> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(GenericDiffUtilCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.insurance_contract_card -> ViewHolder.ContractViewHolder(parent)
        R.layout.dashboard_upsell -> ViewHolder.UpsellViewHolder(parent)
        R.layout.insurance_header -> ViewHolder.TitleViewHolder(parent)
        R.layout.insurance_error -> ViewHolder.Error(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ContractViewHolder -> {
                (items[position] as? InsuranceModel.Contract)?.let {
                    holder.bind(it.inner)
                }
            }
            is ViewHolder.UpsellViewHolder -> {
                (items[position] as? InsuranceModel.Upsell)?.let { holder.bind(it) }
            }
            is ViewHolder.Error -> {
                holder.bind(retry, tracker)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is InsuranceModel.Contract -> R.layout.insurance_contract_card
        is InsuranceModel.Upsell -> R.layout.dashboard_upsell
        is InsuranceModel.Header -> R.layout.insurance_header
        InsuranceModel.Error -> R.layout.insurance_error
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class UpsellViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.dashboard_upsell, parent, false)
        ) {
            private val binding by viewBinding(DashboardUpsellBinding::bind)

            init {
                binding.apply {
                    cta.setHapticClickListener {
                        val intent = ChatActivity.newInstance(cta.context, true)
                        val options =
                            ActivityOptionsCompat.makeCustomAnimation(
                                cta.context,
                                R.anim.activity_slide_up_in,
                                R.anim.stay_in_place
                            )

                        ActivityCompat.startActivity(cta.context, intent, options.toBundle())
                    }
                }
            }

            fun bind(model: InsuranceModel.Upsell) {
                binding.apply {
                    title.text = title.resources.getString(model.title)
                    description.text = description.resources.getString(model.description)
                    cta.text = cta.resources.getString(model.ctaText)
                }
            }
        }

        class ContractViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.insurance_contract_card, parent, false)
        ) {
            private val binding by viewBinding(InsuranceContractCardBinding::bind)

            fun bind(contract: InsuranceQuery.Contract) {
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
                            TypeOfContract.NO_TRAVEL,
                            TypeOfContract.NO_TRAVEL_YOUTH,
                            TypeOfContract.DK_HOME_CONTENT -> {
                                adapter.submitList(
                                    listOf(
                                        ContractModel.ContractType(contract.typeOfContract),
                                        ContractModel.NoOfCoInsured(contract.currentAgreement.numberCoInsured)
                                    )
                                )
                            }
                            TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                            TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                            TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                            TypeOfContract.SE_APARTMENT_STUDENT_RENT -> {
                                adapter.submitList(
                                    listOf(
                                        ContractModel.ContractType(contract.typeOfContract),
                                        ContractModel.Student(contract.typeOfContract),
                                        ContractModel.NoOfCoInsured(contract.currentAgreement.numberCoInsured)
                                    )
                                )
                            }
                            TypeOfContract.UNKNOWN__ -> {
                            }
                        }
                    }
                    root.setHapticClickListener {
                        startActivity(
                            root.context,
                            ContractDetailActivity.newInstance(root.context),
                            null
                        )
                    }
                }
            }
        }

        class TitleViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.insurance_header, parent, false)
        )

        class Error(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.insurance_error)) {
            private val binding by viewBinding(InsuranceErrorBinding::bind)
            fun bind(retry: () -> Unit, tracker: InsuranceTracker): Any? = with(binding) {
                this.retry.setHapticClickListener {
                    tracker.retry()
                    retry()
                }
            }
        }
    }

    companion object {
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

sealed class InsuranceModel {
    object Header : InsuranceModel()

    data class Contract(
        val inner: InsuranceQuery.Contract
    ) : InsuranceModel()

    data class Upsell(
        @get:StringRes val title: Int,
        @get:StringRes val description: Int,
        @get:StringRes val ctaText: Int
    ) : InsuranceModel()

    object Error : InsuranceModel()
}
