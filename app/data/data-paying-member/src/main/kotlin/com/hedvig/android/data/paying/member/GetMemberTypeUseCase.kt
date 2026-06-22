package com.hedvig.android.data.paying.member

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.toContractType
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import octopus.ActiveInsuranceContractTypesQuery

interface GetMemberTypeUseCase {
  suspend fun invoke(): Either<ErrorMessage, MemberType>
}

enum class MemberType {
  QASA_ONLY_MEMBER,

  // in Payments: hide discounts, payment history, member payment details,
  // in Payments: always show payout account;
  // in Payments: show connect payout reminder if missing
  // HelpCenter: hide Payments section
  STANDARD_MEMBER,

  // current logic
  STANDARD_TO_QASA_MEMBER,
  // in Payments: if no upcoming payments: hide discounts and member payment details
  // in Payments: if upcoming payment and missing payin: show connect payin reminder,
  // else show connect payout reminder if missing
  // in Payments: always show payout account;
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetMemberTypeUseCase>())
internal class SwitchingGetMemberTypeUseCase(
  override val demoManager: DemoManager,
  override val demoImpl: GetMemberTypeUseCaseDemo,
  override val prodImpl: GetMemberTypeUseCaseImpl,
) : GetMemberTypeUseCase, DemoSwitcher<GetMemberTypeUseCase> {
  override suspend fun invoke() = pick().invoke()
}

@SingleIn(AppScope::class)
@Inject
internal class GetMemberTypeUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMemberTypeUseCase {
  override suspend fun invoke(): Either<ErrorMessage, MemberType> {
    return either {
      val result = apolloClient.query(ActiveInsuranceContractTypesQuery())
        .safeExecute(::ErrorMessage)
        .bind()
      val activeContractsTypes = result.currentMember
        .activeContracts
        .map { it.currentAgreement.productVariant.typeOfContract.toContractType() }
      val terminatedContractsTypes = result.currentMember
        .terminatedContracts
        .map { it.currentAgreement.productVariant.typeOfContract.toContractType() }

      val onlyQasaContracts = (
        activeContractsTypes.isNotEmpty() ||
          terminatedContractsTypes.isNotEmpty()
      ) &&
        activeContractsTypes.all { it == ContractType.SE_QASA_LANDLORD } &&
        terminatedContractsTypes.all { it == ContractType.SE_QASA_LANDLORD }
      if (onlyQasaContracts) return@either MemberType.QASA_ONLY_MEMBER

      val standardToQasa = activeContractsTypes.isNotEmpty() &&
        activeContractsTypes.all { it == ContractType.SE_QASA_LANDLORD } &&
        terminatedContractsTypes.any { it != ContractType.SE_QASA_LANDLORD }
      if (standardToQasa) return@either MemberType.STANDARD_TO_QASA_MEMBER

      MemberType.STANDARD_MEMBER
    }
  }
}

@SingleIn(AppScope::class)
@Inject
internal class GetMemberTypeUseCaseDemo : GetMemberTypeUseCase {
  override suspend fun invoke(): Either<ErrorMessage, MemberType> {
    return MemberType.STANDARD_MEMBER.right()
  }
}
