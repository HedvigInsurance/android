package com.hedvig.android.feature.payoutaccount

import androidx.compose.runtime.mutableStateListOf
import com.apollographql.apollo.api.DataBuilderScope
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import octopus.GetPayoutMethodsQuery
import octopus.builder.Data
import octopus.builder.MemberPaymentMethodMap
import octopus.builder.buildMember
import octopus.builder.buildMemberPaymentAvailablePaymentMethod
import octopus.builder.buildMemberPaymentMethod
import octopus.builder.buildMemberPaymentMethods
import octopus.builder.buildPaymentMethodBankAccountDetails
import octopus.builder.buildPaymentMethodSwishDetails
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider

internal fun payoutMethodsData(
  payoutMethods: DataBuilderScope.() -> List<MemberPaymentMethodMap> = { emptyList() },
  availablePayoutProviders: List<MemberPaymentProvider> = emptyList(),
  availablePayinOnlyProviders: List<MemberPaymentProvider> = emptyList(),
): GetPayoutMethodsQuery.Data = GetPayoutMethodsQuery.Data(OctopusFakeResolver) {
  currentMember = buildMember {
    paymentMethods = buildMemberPaymentMethods {
      this.payoutMethods = payoutMethods()
      availableMethods = availablePayoutProviders.map { provider ->
        buildMemberPaymentAvailablePaymentMethod {
          this.provider = provider
          supportsPayout = true
        }
      } + availablePayinOnlyProviders.map { provider ->
        buildMemberPaymentAvailablePaymentMethod {
          this.provider = provider
          supportsPayout = false
        }
      }
    }
  }
}

internal fun DataBuilderScope.swishPayoutMethod(
  phoneNumber: String,
  status: MemberPaymentMethodStatus = MemberPaymentMethodStatus.ACTIVE,
  isDefault: Boolean = false,
): MemberPaymentMethodMap = buildMemberPaymentMethod {
  provider = MemberPaymentProvider.SWISH
  this.status = status
  this.isDefault = isDefault
  details = buildPaymentMethodSwishDetails { this.phoneNumber = phoneNumber }
}

internal fun DataBuilderScope.bankPayoutMethod(
  provider: MemberPaymentProvider,
  account: String,
  bank: String,
  status: MemberPaymentMethodStatus = MemberPaymentMethodStatus.ACTIVE,
  isDefault: Boolean = false,
): MemberPaymentMethodMap = buildMemberPaymentMethod {
  this.provider = provider
  this.status = status
  this.isDefault = isDefault
  details = buildPaymentMethodBankAccountDetails {
    this.account = account
    this.bank = bank
  }
}

internal class FakeNetworkCacheManager : NetworkCacheManager {
  var clears = 0

  override suspend fun clearCache() {
    clears++
  }
}

internal class TestBackstack(vararg initial: HedvigNavKey) : Backstack {
  override val entries: MutableList<HedvigNavKey> = mutableStateListOf(*initial)
}

internal data object TestRootKey : HedvigNavKey
