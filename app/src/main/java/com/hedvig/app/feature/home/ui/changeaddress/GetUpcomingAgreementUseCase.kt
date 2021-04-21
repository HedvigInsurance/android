package com.hedvig.app.feature.home.ui.changeaddress

import androidx.annotation.StringRes
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.NoUpcomingAgreementChange
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.stringRes
import java.time.LocalDate

class GetUpcomingAgreementUseCase(
    private val apolloClient: ApolloClient,
) {

    suspend operator fun invoke(): UpcomingAgreementResult {
        return when (val response = apolloClient.query(UpcomingAgreementQuery()).safeQuery()) {
            is QueryResult.Success -> {
                val contracts = response.data?.contracts
                if (contracts.isNullOrEmpty()) {
                    Error.NoContractsError
                } else {
                    contracts
                        .firstOrNull { it.upcomingAgreementChange() != null }
                        ?.upcomingAgreementChange()
                        ?.let { it.newAgreement.toUpComingAgreement() ?: Error.NoContractsError }
                        ?: NoUpcomingAgreementChange
                }
            }
            is QueryResult.Error -> Error.GeneralError(response.message)
        }
    }

    private fun UpcomingAgreementFragment.NewAgreement.toUpComingAgreement(): UpcomingAgreement? {
        return asSwedishApartmentAgreement?.let {
            UpcomingAgreement(
                address = UpcomingAgreement.Address(
                    street = it.address.fragments.addressFragment.street,
                    postalCode = it.address.fragments.addressFragment.postalCode,
                    city = it.address.fragments.addressFragment.city
                ),
                squareMeters = it.squareMeters,
                activeFrom = it.activeFrom,
                addressType = it.saType.stringRes()
            )
        } ?: asSwedishHouseAgreement?.let {
            UpcomingAgreement(
                address = UpcomingAgreement.Address(
                    street = it.address.fragments.addressFragment.street,
                    postalCode = it.address.fragments.addressFragment.postalCode,
                    city = it.address.fragments.addressFragment.city
                ),
                squareMeters = it.squareMeters,
                activeFrom = it.activeFrom,
                addressType = null
            )
        } ?: asNorwegianHomeContentAgreement?.let {
            UpcomingAgreement(
                address = UpcomingAgreement.Address(
                    street = it.address.fragments.addressFragment.street,
                    postalCode = it.address.fragments.addressFragment.postalCode,
                    city = it.address.fragments.addressFragment.city
                ),
                squareMeters = it.squareMeters,
                activeFrom = it.activeFrom,
                addressType = it.nhcType?.stringRes()
            )
        } ?: asDanishHomeContentAgreement?.let {
            UpcomingAgreement(
                address = UpcomingAgreement.Address(
                    street = it.address.fragments.addressFragment.street,
                    postalCode = it.address.fragments.addressFragment.postalCode,
                    city = it.address.fragments.addressFragment.city
                ),
                squareMeters = it.squareMeters,
                activeFrom = it.activeFrom,
                addressType = it.dhcType?.stringRes()
            )
        }
    }

    sealed class UpcomingAgreementResult {
        data class UpcomingAgreement(
            val address: Address,
            val squareMeters: Int,
            val activeFrom: LocalDate?,
            @StringRes
            val addressType: Int?
        ) : UpcomingAgreementResult() {
            data class Address(
                val street: String,
                val postalCode: String,
                val city: String?,
            )
        }

        object NoUpcomingAgreementChange : UpcomingAgreementResult()

        sealed class Error : UpcomingAgreementResult() {
            object NoContractsError : Error()
            data class GeneralError(val message: String?) : Error()
        }
    }
}
