package com.hedvig.android.data.claimflow

import androidx.compose.runtime.Immutable
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.core.uidata.UiNullableMoney
import com.hedvig.android.data.claimflow.model.AudioUrl
import com.hedvig.android.data.claimflow.model.FlowId
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.audio.player.data.SignedAudioUrl
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

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
    val freeTextAvailable: Boolean,
    val freeText: String?,
    val freeTextQuestions: List<String>,
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
    val selectedOptionId: String?,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<LocalContractContractOption>>())
    }
  }

  @Serializable
  data class SingleItem(
    val preferredCurrency: UiCurrencyCode,
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
        typeOf<UiCurrencyCode>(),
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
  data class DeflectIdProtection(
    val title: String,
    val description: String?,
    val partners: List<IdProtectionDeflectPartner>,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<IdProtectionDeflectPartner>>())
    }
  }

  @Serializable
  data class Summary(
    val claimTypeTitle: String,
    val subTitle: String?,
    val selectedLocation: String?,
    val locationOptions: List<LocationOption>,
    val dateOfOccurrence: LocalDate?,
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
    val freeText: String?,
    val selectedContractExposure: String?,
  ) : ClaimFlowDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<List<LocationOption>>(),
        typeOf<LocalDate?>(),
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
  val displayTitle: String,
  val displaySubtitle: String,
)

@Serializable
sealed interface ItemBrand {
  fun asKnown(): Known? = this as? Known

  @Serializable
  data class Known(
    val displayName: String,
    val itemTypeId: String,
    val itemBrandId: String,
  ) : ItemBrand

  @Serializable
  object Unknown : ItemBrand
}

@Serializable
sealed interface ItemModel {
  fun asKnown(): Known? = this as? Known

  fun asNew(): New? = this as? New

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
  object Unknown : ItemModel
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
data class IdProtectionDeflectPartner(
  val title: String?,
  val description: String?,
  val info: String?,
  val partner: DeflectPartner,
  private val urlButtonTitle: String?,
) {
  val buttonsState: ButtonsState = when {
    urlButtonTitle != null && partner.url != null && partner.phoneNumber != null -> ButtonsState.Both(
      phoneNumber = partner.phoneNumber,
      url = partner.url,
      urlButtonTitle = urlButtonTitle,
    )

    urlButtonTitle != null && partner.url != null -> ButtonsState.Url(
      url = partner.url,
      urlButtonTitle = urlButtonTitle,
    )
    partner.phoneNumber != null -> ButtonsState.PhoneNumber(partner.phoneNumber)
    else -> ButtonsState.None
  }

  sealed interface ButtonsState {
    data class Both(val urlButtonTitle: String, val url: String, val phoneNumber: String) : ButtonsState

    data class Url(val urlButtonTitle: String, val url: String) : ButtonsState

    data class PhoneNumber(val phoneNumber: String) : ButtonsState

    data object None : ButtonsState
  }
}

@Serializable
data class DeflectPartner(
  val id: String,
  val imageUrl: String,
  val phoneNumber: String?,
  val url: String?,
  val preferredImageHeight: Int?,
)

@Serializable
data class EmergencyOption(val displayName: String, val value: Boolean)

@Serializable
sealed interface SubmittedContent {
  @Serializable
  data class Audio(val signedAudioURL: SignedAudioUrl) : SubmittedContent
}
