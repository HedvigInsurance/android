package com.hedvig.android.data.claimflow

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.core.uidata.UiNullableMoney
import com.hedvig.android.data.claimflow.ItemBrand.Unknown.displayName
import com.hedvig.android.data.claimflow.ItemModel.Unknown.displayName
import com.hedvig.android.data.claimflow.model.AudioUrl
import com.hedvig.android.data.claimflow.model.FlowId
import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import com.hedvig.audio.player.data.SignedAudioUrl
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.type.CurrencyCode

sealed interface ClaimFlowDestination {
  @Serializable
  object HonestyPledge : ClaimFlowDestination, Destination

  @Serializable
  object NotificationPermission : ClaimFlowDestination, Destination

  @Serializable
  data class AudioRecording(
    val flowId: FlowId,
    val questions: List<String>,
    val audioContent: AudioContent?,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<FlowId>(),
        typeOf<AudioContent?>(),
      )
    }
  }

  @Serializable
  data class DateOfOccurrence(
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate?>(),
        typeOf<LocalDate>(),
      )
    }
  }

  @Serializable
  data class Location(
    val selectedLocation: String?,
    val locationOptions: List<LocationOption>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<LocationOption>>())
    }
  }

  @Serializable
  data class DateOfOccurrencePlusLocation(
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
    val selectedLocation: String?,
    val locationOptions: List<LocationOption>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate?>(),
        typeOf<LocalDate>(),
        typeOf<List<LocationOption>>(),
      )
    }
  }

  @Serializable
  data class PhoneNumber(val phoneNumber: String) : ClaimFlowDestination, Destination

  @Serializable
  data class SelectContract(
    val options: List<LocalContractContractOption>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<LocalContractContractOption>>())
    }
  }

  @Serializable
  data class SingleItem(
    val preferredCurrency: CurrencyCode,
    val purchaseDate: LocalDate?,
    val purchasePrice: UiNullableMoney?,
    val purchasePriceApplicable: Boolean,
    val availableItemBrands: List<ItemBrand>?,
    val selectedItemBrand: String?,
    val availableItemModels: List<ItemModel>?,
    val selectedItemModel: String?,
    val customName: String?,
    val availableItemProblems: List<ItemProblem>?,
    val selectedItemProblems: List<String>?,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<CurrencyCode>(),
        typeOf<LocalDate?>(),
        typeOf<UiNullableMoney?>(),
        typeOf<List<ItemBrand>?>(),
        typeOf<List<ItemModel>?>(),
        typeOf<List<ItemProblem>?>(),
      )
    }
  }

  @Serializable
  data class DeflectGlassDamage(
    val partners: List<DeflectPartner>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<DeflectPartner>>())
    }
  }

  @Serializable
  data class DeflectTowing(
    val partners: List<DeflectPartner>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<DeflectPartner>>())
    }
  }

  @Serializable
  data class DeflectCarOtherDamage(
    val partners: List<DeflectPartner>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<DeflectPartner>>())
    }
  }

  @Serializable
  data class ConfirmEmergency(
    val text: String,
    val confirmEmergency: Boolean?,
    val options: List<EmergencyOption>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<Boolean?>(),
        typeOf<List<EmergencyOption>>(),
      )
    }
  }

  @Serializable
  data class DeflectEmergency(
    val partners: List<DeflectPartner>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<DeflectPartner>>())
    }
  }

  @Serializable
  data class DeflectPests(
    val partners: List<DeflectPartner>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<DeflectPartner>>())
    }
  }

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
    val customName: String?,
    val availableItemProblems: List<ItemProblem>?,
    val selectedItemProblems: List<String>?,
    val submittedContent: SubmittedContent?,
    val files: List<UiFile>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<List<LocationOption>>(),
        typeOf<LocalDate?>(),
        typeOf<LocalDate>(),
        typeOf<CurrencyCode?>(),
        typeOf<UiNullableMoney?>(),
        typeOf<List<ItemBrand>?>(),
        typeOf<List<ItemModel>?>(),
        typeOf<List<ItemProblem>?>(),
        typeOf<SubmittedContent?>(),
        typeOf<List<UiFile>>(),
      )
    }
  }

  @Serializable
  data class SingleItemCheckout(
    val compensation: Compensation,
    val availableCheckoutMethods: List<CheckoutMethod.Known>,
    val modelName: String?,
    val brandName: String?,
    val customName: String?,
  ) : ClaimFlowDestination, Destination {
    @Serializable
    sealed interface Compensation {
      sealed interface Known : Compensation {
        val deductible: UiMoney
        val payoutAmount: UiMoney

        @Serializable
        data class ValueCompensation(
          val price: UiMoney,
          val depreciation: UiMoney,
          override val deductible: UiMoney,
          override val payoutAmount: UiMoney,
        ) : Known

        @Serializable
        data class RepairCompensation(
          val repairCost: UiMoney,
          override val deductible: UiMoney,
          override val payoutAmount: UiMoney,
        ) : Known
      }

      @Serializable
      data object Unknown : Compensation
    }

    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<Compensation>(),
        typeOf<List<CheckoutMethod.Known>>(),
      )
    }
  }

  // Local-only destination, not matching to a flow step, used to handle payout logic
  @Serializable
  data class SingleItemPayout(
    val checkoutMethod: CheckoutMethod.Known,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<CheckoutMethod.Known>())
    }
  }

  @Serializable
  data class FileUpload(
    val title: String,
    val targetUploadUrl: String,
    val uploads: List<UiFile>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<UiFile>>())
    }
  }

  @Serializable
  object ClaimSuccess : ClaimFlowDestination, Destination

  @Serializable
  object Failure : ClaimFlowDestination, Destination

  @Serializable
  object UpdateApp : ClaimFlowDestination, Destination
}

@Serializable
data class LocationOption(
  val value: String,
  val displayName: String,
)

@Serializable
data class LocalContractContractOption(
  val id: String,
  val displayName: String,
)

@Serializable
sealed interface ItemBrand {
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
sealed interface ItemModel {
  fun asKnown(): Known? = this as? Known

  fun asNew(): New? = this as? New

  fun displayName(resources: Resources): String {
    return when (this) {
      is Known -> displayName
      is Unknown -> resources.getString(displayName)
      is New -> displayName
    }
  }

  @Serializable
  data class New(
    val displayName: String,
  ) : ItemModel

  @Serializable
  data class Known(
    val displayName: String,
    val itemTypeId: String,
    val itemBrandId: String,
    val itemModelId: String,
  ) : ItemModel

  @Serializable
  object Unknown : ItemModel {
    @StringRes
    val displayName: Int = hedvig.resources.R.string.claims_item_model_other
  }
}

@Serializable
data class ItemProblem(
  val displayName: String,
  val itemProblemId: String,
)

@Serializable
sealed interface CheckoutMethod {
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
data class AudioContent(
  /**
   * The url to be used to play back the audio file
   */
  val signedUrl: AudioUrl,
  /**
   * The url that the backend expects when trying to go to the next step of the flow
   */
  val audioUrl: AudioUrl,
)

@Serializable
data class DeflectPartner(
  val id: String,
  val imageUrl: String,
  val phoneNumber: String?,
  val url: String?,
)

@Serializable
data class EmergencyOption(val displayName: String, val value: Boolean)

@Serializable
sealed interface SubmittedContent {
  @Serializable
  data class Audio(val signedAudioURL: SignedAudioUrl) : SubmittedContent
}
