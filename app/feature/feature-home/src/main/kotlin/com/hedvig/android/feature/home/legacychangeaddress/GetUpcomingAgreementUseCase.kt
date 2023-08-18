package com.hedvig.android.feature.home.legacychangeaddress

import android.os.Parcelable
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.android.table.Table
import com.hedvig.android.language.LanguageService
import giraffe.UpcomingAgreementQuery
import java.time.LocalDate
import kotlinx.parcelize.Parcelize

class GetUpcomingAgreementUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {

  private fun upcomingAgreementQuery() = UpcomingAgreementQuery(
    locale = languageService.getGraphQLLocale(),
  )

  suspend fun invoke(): UpcomingAgreementResult {
    return when (val response = apolloClient.query(upcomingAgreementQuery()).safeExecute()) {
      is OperationResult.Success -> {
        val contracts = response.data.contracts
        if (contracts.isEmpty()) {
          UpcomingAgreementResult.Error.NoContractsError
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
            ?: UpcomingAgreementResult.NoUpcomingAgreementChange
        }
      }
      is OperationResult.Error -> UpcomingAgreementResult.Error.GeneralError(response.message)
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
