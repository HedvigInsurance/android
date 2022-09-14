package com.hedvig.app.feature.home.ui.changeaddress

import android.os.Parcelable
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.UpcomingAgreementQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.NoUpcomingAgreementChange
import com.hedvig.app.feature.table.Table
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.toUpcomingAgreementResult
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

class GetUpcomingAgreementUseCase(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
) {

  private fun upcomingAgreementQuery() = UpcomingAgreementQuery(
    locale = localeManager.defaultLocale(),
  )

  suspend fun invoke(): UpcomingAgreementResult {
    return when (val response = apolloClient.query(upcomingAgreementQuery()).safeExecute()) {
      is OperationResult.Success -> {
        val contracts = response.data?.contracts
        if (contracts.isNullOrEmpty()) {
          Error.NoContractsError
        } else {
          contracts.firstOrNull {
            it.fragments.upcomingAgreementFragment
              .upcomingAgreementDetailsTable
              .fragments
              .tableFragment
              .sections
              .isNotEmpty()
          }
            ?.fragments
            ?.upcomingAgreementFragment
            ?.toUpcomingAgreementResult()
            ?: NoUpcomingAgreementChange
        }
      }
      is OperationResult.Error -> Error.GeneralError(response.message)
    }
  }

  sealed class UpcomingAgreementResult {

    @Parcelize
    data class UpcomingAgreement(
      val activeFrom: LocalDate?,
      val table: Table?,
    ) : UpcomingAgreementResult(), Parcelable

    object NoUpcomingAgreementChange : UpcomingAgreementResult()

    sealed class Error : UpcomingAgreementResult() {
      object NoContractsError : Error()
      data class GeneralError(val message: String?) : Error()
    }
  }
}
