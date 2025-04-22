package com.hedvig.android.feature.insurance.certificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.InsuranceEvidenceCreateMutation
import octopus.type.InsuranceEvidenceInput

interface GetInsuranceEvidenceUseCase {
  suspend fun invoke(email: String): Either<ErrorMessage, String>
}


internal class  GetInsuranceEvidenceUseCaseImpl(
  val apolloClient: ApolloClient
): GetInsuranceEvidenceUseCase {
  override suspend fun invoke(email: String): Either<ErrorMessage, String> = either {
    val input = InsuranceEvidenceInput(
      email = email,
    )
    val result = apolloClient
      .mutation(InsuranceEvidenceCreateMutation(input))
      .safeExecute(::ErrorMessage)
      .onLeft {
        logcat(
          priority = LogPriority.ERROR,
          throwable = it.throwable,
        ) {
          "GetInsuranceEvidenceUseCase: ${it.message ?: "Could not create insurance evidence"}"
        }
      }
      .getOrNull()
    val userError = result?.insuranceEvidenceCreate?.userError?.message
    val url = result?.insuranceEvidenceCreate?.insuranceEvidenceInformation?.signedUrl
    if (userError!=null) {
      raise(ErrorMessage(userError))
    } else if (url==null) {
      logcat(
        priority = LogPriority.ERROR
      ) {
        "GetInsuranceEvidenceUseCase: Could not create insurance evidence, insuranceEvidenceInformation is null"
      }
      raise(ErrorMessage())
    } else {
      url
    }
  }
}
