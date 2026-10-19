package com.hedvig.android.feature.payin.account

import com.apollographql.apollo.api.DataBuilderScope
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import octopus.GetPayinMethodsQuery
import octopus.builder.Data
import octopus.builder.MemberPaymentMethodMap
import octopus.builder.buildMember
import octopus.builder.buildMemberPaymentAvailablePaymentMethod
import octopus.builder.buildMemberPaymentMethod
import octopus.builder.buildMemberPaymentMethods
import octopus.builder.buildPaymentMethodBankAccountDetails
import octopus.builder.buildPaymentMethodInvoiceDetails
import octopus.builder.buildPaymentMethodSwishDetails
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery

internal fun payinMethodsData(
  payinMethods: DataBuilderScope.() -> List<MemberPaymentMethodMap> = { emptyList() },
  availablePayinProviders: List<MemberPaymentProvider> = emptyList(),
  availablePayoutOnlyProviders: List<MemberPaymentProvider> = emptyList(),
  chargingDay: Int? = 27,
): GetPayinMethodsQuery.Data = GetPayinMethodsQuery.Data(OctopusFakeResolver) {
  currentMember = buildMember {
    paymentMethods = buildMemberPaymentMethods {
      this.chargingDay = chargingDay
      this.payinMethods = payinMethods()
      availableMethods = availablePayinProviders.map { provider ->
        buildMemberPaymentAvailablePaymentMethod {
          this.provider = provider
          supportsPayin = true
          isActive = true
        }
      } + availablePayoutOnlyProviders.map { provider ->
        buildMemberPaymentAvailablePaymentMethod {
          this.provider = provider
          supportsPayin = false
          isActive = true
        }
      }
    }
  }
}

internal fun DataBuilderScope.swishPayinMethod(
  phoneNumber: String,
  status: MemberPaymentMethodStatus = MemberPaymentMethodStatus.ACTIVE,
  isDefault: Boolean = false,
): MemberPaymentMethodMap = buildMemberPaymentMethod {
  provider = MemberPaymentProvider.SWISH
  this.status = status
  this.isDefault = isDefault
  details = buildPaymentMethodSwishDetails { this.phoneNumber = phoneNumber }
}

internal fun DataBuilderScope.trustlyPayinMethod(
  account: String,
  bank: String,
  status: MemberPaymentMethodStatus = MemberPaymentMethodStatus.ACTIVE,
  isDefault: Boolean = false,
): MemberPaymentMethodMap = buildMemberPaymentMethod {
  provider = MemberPaymentProvider.TRUSTLY
  this.status = status
  this.isDefault = isDefault
  details = buildPaymentMethodBankAccountDetails {
    this.account = account
    this.bank = bank
  }
}

internal fun DataBuilderScope.invoicePayinMethod(
  delivery: PaymentMethodInvoiceDelivery,
  email: String?,
  status: MemberPaymentMethodStatus = MemberPaymentMethodStatus.ACTIVE,
  isDefault: Boolean = false,
): MemberPaymentMethodMap = buildMemberPaymentMethod {
  provider = MemberPaymentProvider.INVOICE
  this.status = status
  this.isDefault = isDefault
  details = buildPaymentMethodInvoiceDetails {
    this.delivery = delivery
    this.email = email
  }
}

internal class FakeNetworkCacheManager : NetworkCacheManager {
  var clears = 0

  override suspend fun clearCache() {
    clears++
  }
}
