package com.hedvig.app.feature.home.ui.changeaddress

import android.os.Parcelable
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.NoUpcomingAgreementChange
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
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
                    contracts.firstOrNull { it.upcomingAgreementDetailsTable.sections.isNotEmpty() }
                        ?.toUpcomingAgreementResult()
                        ?: NoUpcomingAgreementChange
                }
            }
            is QueryResult.Error -> Error.GeneralError(response.message)
        }
    }

    private fun UpcomingAgreementQuery.Contract.toUpcomingAgreementResult(): UpcomingAgreementResult.UpcomingAgreement {
        return UpcomingAgreementResult.UpcomingAgreement(
            activeFrom = activeFrom(),
            address = address(),
            table = createTable()
        )
    }

    private fun UpcomingAgreementQuery.Contract.createTable() =
        UpcomingAgreementResult.UpcomingAgreement.UpcomingAgreementTable(
            title = upcomingAgreementDetailsTable.title,
            sections = upcomingAgreementDetailsTable.sections.map { section ->
                UpcomingAgreementResult.UpcomingAgreement.UpcomingAgreementTable.Section(
                    title = section.title,
                    rows = section.rows.map { row ->
                        UpcomingAgreementResult.UpcomingAgreement.UpcomingAgreementTable.Row(
                            title = row.title,
                            value = row.value,
                            subTitle = row.subtitle
                        )
                    })
            }
        )

    private fun UpcomingAgreementQuery.Contract.activeFrom(): LocalDate? {
        val newAgreement = newAgreement()

        return newAgreement?.asNorwegianHomeContentAgreement?.activeFrom
            ?: newAgreement?.asDanishHomeContentAgreement?.activeFrom
            ?: newAgreement?.asSwedishApartmentAgreement?.activeFrom
            ?: newAgreement?.asSwedishHouseAgreement?.activeFrom
    }

    private fun UpcomingAgreementQuery.Contract.address(): String? {
        val newAgreement = newAgreement()

        return newAgreement?.asNorwegianHomeContentAgreement?.address?.fragments?.addressFragment?.street
            ?: newAgreement?.asDanishHomeContentAgreement?.address?.fragments?.addressFragment?.street
            ?: newAgreement?.asSwedishApartmentAgreement?.address?.fragments?.addressFragment?.street
            ?: newAgreement?.asSwedishHouseAgreement?.address?.fragments?.addressFragment?.street
    }

    private fun UpcomingAgreementQuery.Contract.newAgreement(): UpcomingAgreementFragment.NewAgreement? {
        return status.asActiveStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementFragment?.newAgreement
            ?: status.asTerminatedInFutureStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementFragment?.newAgreement
            ?: status.asTerminatedTodayStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementFragment?.newAgreement
    }

    sealed class UpcomingAgreementResult {

        @Parcelize
        data class UpcomingAgreement(
            val activeFrom: LocalDate?,
            val address: String?,
            val table: UpcomingAgreementTable
        ) : UpcomingAgreementResult(), Parcelable {
            @Parcelize
            data class UpcomingAgreementTable(
                val title: String,
                val sections: List<Section>
            ) : Parcelable {
                @Parcelize
                data class Section(
                    val title: String,
                    val rows: List<Row>
                ) : Parcelable

                @Parcelize
                data class Row(
                    val title: String,
                    val subTitle: String?,
                    val value: String
                ) : Parcelable
            }
        }

        object NoUpcomingAgreementChange : UpcomingAgreementResult()

        sealed class Error : UpcomingAgreementResult() {
            object NoContractsError : Error()
            data class GeneralError(val message: String?) : Error()
        }
    }
}
