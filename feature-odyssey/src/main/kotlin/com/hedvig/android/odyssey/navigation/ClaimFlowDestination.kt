package com.hedvig.android.odyssey.navigation

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.hedvig.android.odyssey.model.FlowId
import com.hedvig.android.odyssey.navigation.ItemBrand.Unknown.displayName
import com.hedvig.android.odyssey.navigation.ItemModel.Unknown.displayName
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode

internal sealed interface Destinations : Destination {
  @Serializable
  object ClaimFlow : Destinations
}

internal sealed interface ClaimFlowDestination : Destination {
  @Serializable
  object HonestyPledge : ClaimFlowDestination

  @Serializable
  object NotificationPermission : ClaimFlowDestination

  @Serializable
  data class AudioRecording(val flowId: FlowId, val questions: List<String>) : ClaimFlowDestination

  @Serializable
  data class DateOfOccurrence(
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
  ) : ClaimFlowDestination

  @Serializable
  data class Location(
    val selectedLocation: String?,
    val locationOptions: List<LocationOption>,
  ) : ClaimFlowDestination

  @Serializable
  data class DateOfOccurrencePlusLocation(
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
    val selectedLocation: String?,
    val locationOptions: List<LocationOption>,
  ) : ClaimFlowDestination

  @Serializable
  data class PhoneNumber(val phoneNumber: String) : ClaimFlowDestination

  @Serializable
  data class SingleItem(
    val preferredCurrency: CurrencyCode,
    val purchaseDate: LocalDate?,
    val purchasePrice: UiNullableMoney?,
    val availableItemBrands: List<ItemBrand>?,
    val selectedItemBrand: String?,
    val availableItemModels: List<ItemModel>?,
    val selectedItemModel: String?,
    val availableItemProblems: List<ItemProblem>?,
    val selectedItemProblems: List<String>?,
  ) : ClaimFlowDestination

  @Serializable
  data class Summary(
    val claimTypeTitle: String,
    val selectedLocation: String?,
    val locationOptions: List<LocationOption>,
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
    val preferredCurrency: CurrencyCode?,
    val purchaseDate: LocalDate?,
    val purchasePrice: UiNullableMoney?,
    val availableItemBrands: List<ItemBrand>?,
    val selectedItemBrand: String?,
    val availableItemModels: List<ItemModel>?,
    val selectedItemModel: String?,
    val availableItemProblems: List<ItemProblem>?,
    val selectedItemProblems: List<String>?,
  ) : ClaimFlowDestination

  @Serializable
  data class SingleItemCheckout(
    val price: UiMoney,
    val depreciation: UiMoney,
    val deductible: UiMoney,
    val payoutAmount: UiMoney,
    val availableCheckoutMethods: List<CheckoutMethod.Known>,
  ) : ClaimFlowDestination

  @Serializable
  object ClaimSuccess : ClaimFlowDestination

  @Serializable
  object Failure : ClaimFlowDestination

  @Serializable
  object UpdateApp : ClaimFlowDestination
}

@Serializable
internal data class LocationOption(
  val value: String,
  val displayName: String,
)

@Serializable
internal sealed interface ItemBrand {
  fun asKnown(): Known? = this as? Known

  fun displayName(resources: Resources): String {
    return when (this) {
      is Known -> displayName
      is Unknown -> resources.getString(displayName)
    }
  }

  @Serializable
  data class Known(
    val displayName: String,
    val itemTypeId: String,
    val itemBrandId: String,
  ) : ItemBrand

  @Serializable
  object Unknown : ItemBrand {
    @StringRes
    val displayName: Int = hedvig.resources.R.string.GENERAL_NOT_SURE
  }
}

@Serializable
internal sealed interface ItemModel {
  fun asKnown(): Known? = this as? Known

  fun displayName(resources: Resources): String {
    return when (this) {
      is Known -> displayName
      is Unknown -> resources.getString(displayName)
    }
  }

  @Serializable
  data class Known(
    val displayName: String,
    val imageUrl: String?,
    val itemTypeId: String,
    val itemBrandId: String,
    val itemModelId: String,
  ) : ItemModel

  @Serializable
  object Unknown : ItemModel {
    @StringRes
    val displayName: Int = hedvig.resources.R.string.GENERAL_NOT_SURE
  }
}

@Serializable
internal data class ItemProblem(
  val displayName: String,
  val itemProblemId: String,
)

@Serializable
internal sealed interface CheckoutMethod {

  @Serializable
  sealed interface Known : CheckoutMethod {
    val id: String
    val displayName: String
    val uiMoney: UiMoney

    @Serializable
    data class AutomaticAutogiro(
      override val id: String,
      override val displayName: String,
      override val uiMoney: UiMoney,
    ) : Known
  }

  object Unknown : CheckoutMethod
}

@Immutable
@Serializable
internal data class UiNullableMoney(val amount: Double?, val currencyCode: CurrencyCode) {
  override fun toString(): String {
    return "${amount ?: "-"} $currencyCode"
  }

  companion object {
    fun fromMoneyFragment(fragment: MoneyFragment?): UiNullableMoney? {
      fragment ?: return null
      return UiNullableMoney(fragment.amount, fragment.currencyCode)
    }
  }
}

@Immutable
@Serializable
internal data class UiMoney(val amount: Double, val currencyCode: CurrencyCode) {
  override fun toString(): String {
    return "$amount $currencyCode"
  }

  companion object {
    fun fromMoneyFragment(fragment: MoneyFragment): UiMoney {
      return UiMoney(fragment.amount, fragment.currencyCode)
    }
  }
}
