package com.hedvig.android.feature.addon.purchase.ui.customize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.addon.purchase.data.AddonOffer
import com.hedvig.android.feature.addon.purchase.data.AddonOfferDeflectType
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.feature.addon.purchase.data.GenerateAddonOfferResult
import com.hedvig.android.feature.addon.purchase.data.GetAddonOfferUseCase
import com.hedvig.android.feature.addon.purchase.navigation.AddonType
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.customize.updateTotalExtraForSelectedToggleable
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.datetime.LocalDate

internal class CustomizeAddonViewModel(
  insuranceId: String,
  preselectedAddonDisplayNames: List<String>,
  getAddonOfferUseCase: GetAddonOfferUseCase,
) : MoleculeViewModel<CustomizeTravelAddonEvent, CustomizeAddonState>(
    initialState = CustomizeAddonState.Loading,
    presenter = CustomizeTravelAddonPresenter(
      insuranceId = insuranceId,
      preselectedAddonDisplayNames = preselectedAddonDisplayNames,
      getAddonOfferUseCase = getAddonOfferUseCase,
    ),
  )

internal class CustomizeTravelAddonPresenter(
  private val insuranceId: String,
  private val preselectedAddonDisplayNames: List<String>,
  private val getAddonOfferUseCase: GetAddonOfferUseCase,
) : MoleculePresenter<CustomizeTravelAddonEvent, CustomizeAddonState> {
  @Composable
  override fun MoleculePresenterScope<CustomizeTravelAddonEvent>.present(
    lastState: CustomizeAddonState,
  ): CustomizeAddonState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var selectedOptionInDialog by remember {
      mutableStateOf<AddonQuote?>(
        if (lastState is CustomizeAddonState.Success.Selectable) {
          lastState.currentlyChosenOptionInDialog
        } else {
          null
        },
      )
    }
    val selectedToggleableOptions = remember {
      mutableStateListOf(
        *(
          (lastState as? CustomizeAddonState.Success.Toggleable)
            ?.currentlyChosenOptions ?: emptyList()
        ).toTypedArray(),
      )
    }

    CollectEvents { event ->
      when (event) {
        CustomizeTravelAddonEvent.Reload -> {
          loadIteration++
        }

        is CustomizeTravelAddonEvent.ChooseOptionInDialog -> {
          selectedOptionInDialog = event.option
        }

        CustomizeTravelAddonEvent.ChooseSelectedOption -> {
          val state = currentState as? CustomizeAddonState.Success.Selectable ?: return@CollectEvents
          val optionInDialog = selectedOptionInDialog ?: return@CollectEvents
          currentState = state.copy(
            currentlyChosenOption = optionInDialog,
          )
        }

        CustomizeTravelAddonEvent.SetSelectedOptionBackToPreviouslyChosen -> {
          val state = currentState as? CustomizeAddonState.Success.Selectable ?: return@CollectEvents
          selectedOptionInDialog = state.currentlyChosenOption
        }

        CustomizeTravelAddonEvent.ClearNavigation -> {
          val state = currentState as? CustomizeAddonState.Success ?: return@CollectEvents
          currentState = when (state) {
            is CustomizeAddonState.Success.Selectable -> state.copy(
              commonParams = state.commonParams.copy(
                summaryParamsToNavigateFurther = null,
              ),
            )

            is CustomizeAddonState.Success.Toggleable -> state.copy(
              commonParams = state.commonParams.copy(
                summaryParamsToNavigateFurther = null,
              ),
            )
          }
        }

        CustomizeTravelAddonEvent.SubmitSelected -> {
          val state = currentState as? CustomizeAddonState.Success.Selectable ?: return@CollectEvents
          val summaryParams = SummaryParameters(
            chosenQuotes = listOf(state.currentlyChosenOption),
            activationDate = state.commonParams.activationDate,
            currentlyActiveAddons = state.currentlyActiveAddon?.let {
              listOf(it)
            } ?: emptyList(),
            quoteId = state.commonParams.quoteId,
            baseInsuranceCost = state.commonParams.baseQuoteCost,
            notificationMessage = state.commonParams.notificationMessage,
            contractId = state.commonParams.contractId,
            productVariant = state.commonParams.productVariant,
            addonType = AddonType.SELECTABLE,
          )
          currentState = state.copy(
            commonParams = state.commonParams.copy(
              summaryParamsToNavigateFurther = summaryParams,
            ),
          )
        }

        CustomizeTravelAddonEvent.SubmitToggled -> {
          val state = currentState as? CustomizeAddonState.Success.Toggleable ?: return@CollectEvents
          val summaryParams = SummaryParameters(
            productVariant = state.commonParams.productVariant,
            chosenQuotes = selectedToggleableOptions,
            activationDate = state.commonParams.activationDate,
            currentlyActiveAddons = state.currentlyActiveAddons,
            quoteId = state.commonParams.quoteId,
            baseInsuranceCost = state.commonParams.baseQuoteCost,
            notificationMessage = state.commonParams.notificationMessage,
            contractId = state.commonParams.contractId,
            addonType = AddonType.TOGGLEABLE,
          )
          currentState = state.copy(
            commonParams = state.commonParams.copy(
              summaryParamsToNavigateFurther = summaryParams,
            ),
          )
        }

        is CustomizeTravelAddonEvent.ToggleOption -> {
          Snapshot.withMutableSnapshot {
            if (selectedToggleableOptions.contains(event.option)) {
              selectedToggleableOptions.remove(event.option)
            } else {
              selectedToggleableOptions.add(event.option)
            }
          }
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (currentState is CustomizeAddonState.Success) return@LaunchedEffect
      currentState = CustomizeAddonState.Loading
      getAddonOfferUseCase.invoke(insuranceId).fold(
        ifLeft = {
          currentState = CustomizeAddonState.Failure.GeneralFailure
        },
        ifRight = { result ->
          when (result) {
            is GenerateAddonOfferResult.AddonOfferDeflect -> {
              currentState = CustomizeAddonState.Failure.SpecificDeflect(
                title = result.pageTitle,
                description = result.pageDescription,
                type = result.type,
                contractId = insuranceId,
              )
            }

            is GenerateAddonOfferResult.AddonOfferResult -> {
              val commonParams =
                CommonSuccessParameters(
                  pageTitle = result.pageTitle,
                  pageDescription = result.pageDescription,
                  currentTotalCost = result.currentTotalCost,
                  umbrellaDisplayTitle = result.umbrellaAddonQuote.displayTitle,
                  umbrellaDisplayDescription = result.umbrellaAddonQuote.displayDescription,
                  quoteId = result.umbrellaAddonQuote.quoteId,
                  activationDate = result.umbrellaAddonQuote.activationDate,
                  baseQuoteCost = result.umbrellaAddonQuote.baseInsuranceCost,
                  notificationMessage = result.notificationMessage,
                  productVariant = result.umbrellaAddonQuote.productVariant,
                  contractId = result.contractId,
                  summaryParamsToNavigateFurther = null,
                  whatsIncludedPageTitle = result.whatsIncludedPageTitle,
                  whatsIncludedPageDescription = result.whatsIncludedPageDescription,
                )
              when (val addonOffer = result.umbrellaAddonQuote.addonOffer) {
                is AddonOffer.Selectable -> {
                  val chosenDefault = addonOffer.addonOptions[0]
                  val currentAddon = result.umbrellaAddonQuote.activeAddons.firstOrNull()
                  selectedOptionInDialog = chosenDefault
                  val extra = updateExtraForSelectable(
                    currentAddon,
                    selectedOptionInDialog,
                  )
                  currentState = CustomizeAddonState.Success.Selectable(
                    addonOffer = addonOffer,
                    currentlyChosenOption = chosenDefault,
                    currentlyChosenOptionInDialog = selectedOptionInDialog,
                    chosenOptionPremiumExtra = extra,
                    currentlyActiveAddon = currentAddon,
                    commonParams = commonParams,
                  )
                }

                is AddonOffer.Toggleable -> {
                  val preselectedAddon = addonOffer.addonOptions.firstOrNull { it.displayTitle in preselectedAddonDisplayNames }
                  if (preselectedAddon != null) {
                    selectedToggleableOptions.add(preselectedAddon)
                  }
                  currentState = CustomizeAddonState.Success.Toggleable(
                    commonParams = commonParams,
                    addonOffer = addonOffer,
                    currentlyActiveAddons = result.umbrellaAddonQuote.activeAddons,
                    currentlyChosenOptions = emptyList(),
                    totalPremiumExtra = null,
                  )
                }
              }
            }
          }
        },
      )
    }

    return when (val state = currentState) {
      is CustomizeAddonState.Failure, is CustomizeAddonState.Loading -> state

      is CustomizeAddonState.Success.Selectable -> state.copy(
        currentlyChosenOptionInDialog = selectedOptionInDialog,
        chosenOptionPremiumExtra = updateExtraForSelectable(
          state.currentlyActiveAddon,
          selectedOptionInDialog,
        ),
      )

      is CustomizeAddonState.Success.Toggleable -> state.copy(
        currentlyChosenOptions = selectedToggleableOptions,
        totalPremiumExtra = selectedToggleableOptions.updateTotalExtraForSelectedToggleable(),
      )
    }
  }
}

private fun updateExtraForSelectable(
  currentlyActiveAddon: CurrentlyActiveAddon?,
  chosenAddonQuote: AddonQuote?,
): UiMoney {
  return if (chosenAddonQuote == null) {
    // shouldn't happen
    UiMoney(0.0, UiCurrencyCode.SEK)
  } else if (currentlyActiveAddon == null) {
    chosenAddonQuote.itemCost.monthlyGross
  } else {
    val sum = chosenAddonQuote.itemCost.monthlyGross.amount - currentlyActiveAddon.cost.monthlyGross.amount
    UiMoney(sum, chosenAddonQuote.itemCost.monthlyGross.currencyCode)
  }
}

private fun List<AddonQuote>.updateTotalExtraForSelectedToggleable(): UiMoney? {
  if (this.isEmpty()) {
    return null
  } else {
    val sum = this.sumOf {
      it.itemCost.monthlyGross.amount
    }
    val currency = this.first().itemCost.monthlyGross.currencyCode
    return UiMoney(sum, currency)
  }
}

internal sealed interface CustomizeAddonState {
  data object Loading : CustomizeAddonState

  sealed interface Success : CustomizeAddonState {
    val commonParams: CommonSuccessParameters

    data class Selectable(
      override val commonParams: CommonSuccessParameters,
      val addonOffer: AddonOffer.Selectable,
      val currentlyChosenOption: AddonQuote,
      val currentlyChosenOptionInDialog: AddonQuote?,
      val chosenOptionPremiumExtra: UiMoney,
      val currentlyActiveAddon: CurrentlyActiveAddon?,
    ) : Success

    data class Toggleable(
      override val commonParams: CommonSuccessParameters,
      val addonOffer: AddonOffer.Toggleable,
      val currentlyChosenOptions: List<AddonQuote>,
      val totalPremiumExtra: UiMoney?,
      val currentlyActiveAddons: List<CurrentlyActiveAddon>,
    ) : Success
  }

  sealed interface Failure : CustomizeAddonState {
    data object GeneralFailure : Failure

    data class SpecificDeflect(
      val title: String,
      val description: String,
      val type: AddonOfferDeflectType,
      val contractId: String,
    ) : Failure
  }
}

internal data class CommonSuccessParameters(
  val pageTitle: String,
  val pageDescription: String,
  val currentTotalCost: ItemCost,
  val umbrellaDisplayTitle: String,
  val umbrellaDisplayDescription: String,
  val quoteId: String,
  val activationDate: LocalDate,
  val baseQuoteCost: ItemCost,
  val summaryParamsToNavigateFurther: SummaryParameters?,
  val notificationMessage: String?,
  val productVariant: ProductVariant,
  val contractId: String,
  val whatsIncludedPageTitle: String,
  val whatsIncludedPageDescription: String,
)

internal sealed interface CustomizeTravelAddonEvent {
  data object Reload : CustomizeTravelAddonEvent

  data class ChooseOptionInDialog(val option: AddonQuote) : CustomizeTravelAddonEvent

  data object ChooseSelectedOption : CustomizeTravelAddonEvent

  data object SetSelectedOptionBackToPreviouslyChosen : CustomizeTravelAddonEvent

  data object ClearNavigation : CustomizeTravelAddonEvent

  data object SubmitSelected : CustomizeTravelAddonEvent

  data class ToggleOption(val option: AddonQuote) : CustomizeTravelAddonEvent

  data object SubmitToggled : CustomizeTravelAddonEvent
}
