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
import com.hedvig.android.navigation.compose.typed.SerializableImmutableList
import com.hedvig.audio.player.data.SignedAudioUrl
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.type.CurrencyCode

sealed interface ClaimFlowDestination : Destination {
  @Serializable
  object HonestyPledge : ClaimFlowDestination

  @Serializable
  object NotificationPermission : ClaimFlowDestination

  @Serializable
  data class AudioRecording(
    val flowId: FlowId,
    val questions: List<String>,
    val audioContent: AudioContent?,
  ) : ClaimFlowDestination

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
  data class SelectContract(
    val options: List<LocalContractContractOption>,
  ) : ClaimFlowDestination

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
  data class DeflectGlassDamage(
    val partners: SerializableImmutableList<DeflectPartner>,
  ) : ClaimFlowDestination

  @Serializable
  data class ConfirmEmergency(
    val text: String,
    val confirmEmergency: Boolean?,
    val options: List<EmergencyOption>,
  ) : ClaimFlowDestination

  @Serializable
  data class DeflectEmergency(
    val partners: SerializableImmutableList<DeflectPartner>,
  ) : ClaimFlowDestination

  @Serializable
  data class DeflectPests(
    val partners: SerializableImmutableList<DeflectPartner>,
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
    val submittedContent: SubmittedContent?,
    val files: List<UiFile>,
  ) : ClaimFlowDestination

  @Serializable
  data class SingleItemCheckout(
    val price: UiMoney,
    val depreciation: UiMoney,
    val deductible: UiMoney,
    val payoutAmount: UiMoney,
    val availableCheckoutMethods: List<CheckoutMethod.Known>,
  ) : ClaimFlowDestination

  // Local-only destination, not matching to a flow step, used to handle payout logic
  @Serializable
  data class SingleItemPayout(
    val checkoutMethod: CheckoutMethod.Known,
  ) : ClaimFlowDestination

  @Serializable
  data class FileUpload(
    val title: String,
    val targetUploadUrl: String,
    val uploads: SerializableImmutableList<UiFile>,
  ) : ClaimFlowDestination

  @Serializable
  object ClaimSuccess : ClaimFlowDestination

  @Serializable
  object Failure : ClaimFlowDestination

  @Serializable
  object UpdateApp : ClaimFlowDestination
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
    val itemModelId: String,
  ) : ItemModel

  @Serializable
  object Unknown : ItemModel {
    @StringRes
    val displayName: Int = hedvig.resources.R.string.GENERAL_NOT_SURE
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
