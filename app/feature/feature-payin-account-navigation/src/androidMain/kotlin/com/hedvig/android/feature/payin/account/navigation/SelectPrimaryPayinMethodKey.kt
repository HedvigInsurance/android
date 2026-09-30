package com.hedvig.android.feature.payin.account.navigation

import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

/**
 * The picker for which connected method the member is charged on. Android-only because it carries
 * [PayinAccount], which comes from the JVM-only `:data-paying-member`.
 */
@Serializable
data class SelectPrimaryPayinMethodKey(
  val currentMethods: List<PayinAccount>,
) : HedvigNavKey
