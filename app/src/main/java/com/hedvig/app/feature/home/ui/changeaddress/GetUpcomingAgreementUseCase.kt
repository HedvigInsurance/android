package com.hedvig.app.feature.home.ui.changeaddress

import android.os.Parcelable
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
import kotlinx.android.parcel.Parcelize
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
                addressType = it.saType.stringRes(),
                nrOfCoInsured = it.numberCoInsured
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
                addressType = null,
                nrOfCoInsured = it.numberCoInsured,
                yearBuilt = it.yearOfConstruction,
                numberOfBaths = it.numberOfBathrooms,
                partlySubleted = it.isSubleted,
                ancillaryArea = it.ancillaryArea,
                extraBuildings = it.extraBuildings.mapNotNull { it?.asExtraBuildingCore }.map {
                    UpcomingAgreement.Building(
                        name = it.displayName,
                        area = it.area,
                        hasWaterConnected = it.hasWaterConnected
                    )
                }
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
                addressType = it.nhcType?.stringRes(),
                nrOfCoInsured = it.numberCoInsured
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
                addressType = it.dhcType?.stringRes(),
                nrOfCoInsured = it.numberCoInsured
            )
        }
    }

    sealed class UpcomingAgreementResult {
        @Parcelize
        data class UpcomingAgreement(
            val address: Address,
            val squareMeters: Int,
            val activeFrom: LocalDate?,
            @StringRes
            val addressType: Int?,
            val nrOfCoInsured: Int,
            val yearBuilt: Int? = null,
            val numberOfBaths: Int? = null,
            val partlySubleted: Boolean? = null,
            val ancillaryArea: Int? = null,
            val extraBuildings: List<Building?> = emptyList()
        ) : UpcomingAgreementResult(), Parcelable {
            @Parcelize
            data class Address(
                val street: String,
                val postalCode: String,
                val city: String?,
            ) : Parcelable

            @Parcelize
            data class Building(
                val name: String,
                val area: Int,
                val hasWaterConnected: Boolean
            ) : Parcelable
        }

        object NoUpcomingAgreementChange : UpcomingAgreementResult()

        sealed class Error : UpcomingAgreementResult() {
            object NoContractsError : Error()
            data class GeneralError(val message: String?) : Error()
        }
    }
}
