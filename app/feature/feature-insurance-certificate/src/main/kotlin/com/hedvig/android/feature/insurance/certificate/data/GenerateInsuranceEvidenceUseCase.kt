package com.hedvig.android.feature.insurance.certificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.InsuranceEvidenceCreateMutation
import octopus.type.InsuranceEvidenceInput

internal interface GenerateInsuranceEvidenceUseCase {
  suspend fun invoke(email: String): Either<ErrorMessage, String>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GenerateInsuranceEvidenceUseCaseImpl(
  val apolloClient: ApolloClient,
) : GenerateInsuranceEvidenceUseCase {
  override suspend fun invoke(email: String): Either<ErrorMessage, String> = either {
    val input = InsuranceEvidenceInput(
      email = email,
    )
    val result = apolloClient
      .mutation(InsuranceEvidenceCreateMutation(input))
      .safeExecute()
      .onLeft {
        logcat(
          priority = LogPriority.ERROR,
          operationError = it,
        ) {
          "GetInsuranceEvidenceUseCase: $it"
        }
      }
      .getOrNull()
    val userError = result?.insuranceEvidenceCreate?.userError?.message
    val url = result?.insuranceEvidenceCreate?.insuranceEvidenceInformation?.signedUrl
    if (userError != null) {
      raise(ErrorMessage(userError))
    } else if (url == null) {
      logcat(
        priority = LogPriority.ERROR,
      ) {
        "GetInsuranceEvidenceUseCase: Could not create insurance evidence, insuranceEvidenceInformation is null"
      }
      raise(ErrorMessage())
    } else {
      url
    }
  }
}
