package com.hedvig.app.feature.insurance.ui

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import e
import java.time.format.DateTimeFormatter

fun InsuranceQuery.Contract.bindTo(binding: InsuranceContractCardBinding) =
    binding.apply {
        status.fragments.contractStatusFragment.let { contractStatus ->
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
                when (typeOfContract) {
                    TypeOfContract.SE_APARTMENT_BRF,
                    TypeOfContract.SE_APARTMENT_RENT,
                    TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                    TypeOfContract.SE_APARTMENT_STUDENT_RENT,
                    -> {
                        container.setBackgroundResource(R.drawable.card_apartment_background)
                        blur.setColorFilter(blur.context.compatColor(R.color.color_card_blur_apartment))
                    }
                    TypeOfContract.SE_HOUSE,
                    TypeOfContract.NO_HOME_CONTENT_OWN,
                    TypeOfContract.NO_HOME_CONTENT_RENT,
                    TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                    TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                    TypeOfContract.DK_HOME_CONTENT_OWN,
                    TypeOfContract.DK_HOME_CONTENT_RENT,
                    TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
                    TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT -> {
                        container.setBackgroundResource(R.drawable.card_house_background)
                        blur.setColorFilter(blur.context.compatColor(R.color.color_card_blur_house))
                    }
                    TypeOfContract.NO_TRAVEL,
                    TypeOfContract.NO_TRAVEL_YOUTH,
                    TypeOfContract.DK_TRAVEL,
                    TypeOfContract.DK_TRAVEL_STUDENT -> {
                        container.setBackgroundResource(R.drawable.card_travel_background)
                        blur.setColorFilter(blur.context.compatColor(R.color.color_card_blur_travel))
                    }
                    TypeOfContract.DK_ACCIDENT,
                    TypeOfContract.DK_ACCIDENT_STUDENT -> {
                        container.setBackgroundResource(R.drawable.card_accident_background)
                        blur.setColorFilter(blur.context.compatColor(R.color.color_card_blur_accident))
                    }
                    TypeOfContract.UNKNOWN__ -> {

                    }
                }
            } ?: run {
                container.setBackgroundResource(R.color.color_card_inactive)
                blur.remove()
            }
        }

        contractName.text = displayName
        contractPills.adapter = ContractPillAdapter().also { adapter ->
            when (typeOfContract) {
                TypeOfContract.SE_HOUSE,
                TypeOfContract.SE_APARTMENT_BRF,
                TypeOfContract.SE_APARTMENT_RENT,
                TypeOfContract.NO_HOME_CONTENT_OWN,
                TypeOfContract.NO_HOME_CONTENT_RENT,
                TypeOfContract.DK_HOME_CONTENT_OWN,
                TypeOfContract.DK_HOME_CONTENT_RENT,
                TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
                TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
                TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                TypeOfContract.SE_APARTMENT_STUDENT_RENT -> {
                    adapter.submitList(
                        listOf(
                            ContractModel.Address(currentAgreement),
                            ContractModel.NoOfCoInsured(currentAgreement.numberCoInsured)
                        )
                    )
                }
                TypeOfContract.NO_TRAVEL,
                TypeOfContract.NO_TRAVEL_YOUTH,
                TypeOfContract.DK_TRAVEL,
                TypeOfContract.DK_TRAVEL_STUDENT,
                TypeOfContract.DK_ACCIDENT,
                TypeOfContract.DK_ACCIDENT_STUDENT -> {
                    adapter.submitList(listOf(ContractModel.NoOfCoInsured(currentAgreement.numberCoInsured)))
                }
                TypeOfContract.UNKNOWN__ -> {
                }
            }
        }
        // Prevent this `RecyclerView` from eating clicks in the parent `MaterialCardView`.
        // Alternative implementation path: extend `RecyclerView` and make `onTouchEvent` always return `false`.
        contractPills.suppressLayout(true)
    }

private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu")

private val InsuranceQuery.CurrentAgreement.numberCoInsured: Int
    get() {
        asNorwegianTravelAgreement?.numberCoInsured?.let { return it }
        asSwedishHouseAgreement?.numberCoInsured?.let { return it }
        asSwedishApartmentAgreement?.numberCoInsured?.let { return it }
        asNorwegianHomeContentAgreement?.numberCoInsured?.let { return it }
        asDanishHomeContentAgreement?.numberCoInsured?.let { return it }
        asDanishTravelAgreement?.numberCoInsured?.let { return it }
        asDanishAccidentAgreement?.numberCoInsured?.let { return it }
        e { "Unable to infer amount coinsured for agreement: $this" }
        return 0
    }
