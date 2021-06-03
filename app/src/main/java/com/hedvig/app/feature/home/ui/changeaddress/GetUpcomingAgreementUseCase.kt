package com.hedvig.app.feature.home.ui.changeaddress

import android.os.Parcelable
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.NoUpcomingAgreementChange
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.toUpcomingAgreementResult
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

class GetUpcomingAgreementUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {

    private fun upcomingAgreementQuery() = UpcomingAgreementQuery(
        locale = localeManager.defaultLocale()
    )

    suspend operator fun invoke(): UpcomingAgreementResult {
        return when (val response = apolloClient.query(upcomingAgreementQuery()).safeQuery()) {
            is QueryResult.Success -> {
                val contracts = response.data?.contracts
                if (contracts.isNullOrEmpty()) {
                    Error.NoContractsError
                } else {
                    contracts.firstOrNull {
                        it.fragments.upcomingAgreementFragment.upcomingAgreementDetailsTable != null
                    }
                        ?.fragments
                        ?.upcomingAgreementFragment
                        ?.toUpcomingAgreementResult()
                        ?: NoUpcomingAgreementChange
                }
            }
            is QueryResult.Error -> Error.GeneralError(response.message)
        }
    }

    sealed class UpcomingAgreementResult {

        @Parcelize
        data class UpcomingAgreement(
            val activeFrom: LocalDate?,
            val address: String?,
            val table: Table?,
        ) : UpcomingAgreementResult(), Parcelable {
            @Parcelize
            data class Table(
                val title: String,
                val sections: List<Section>,
            ) : Parcelable {
                @Parcelize
                data class Section(
                    val title: String,
                    val rows: List<Row>,
                ) : Parcelable

                @Parcelize
                data class Row(
                    val title: String,
                    val subtitle: String?,
                    val value: String,
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
