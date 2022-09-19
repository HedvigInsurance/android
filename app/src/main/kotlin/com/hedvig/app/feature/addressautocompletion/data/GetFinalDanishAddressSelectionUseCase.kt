package com.hedvig.app.feature.addressautocompletion.data

import arrow.core.Either
import arrow.core.getOrElse
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.type.AddressAutocompleteType
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress

/**
 * Returns a [DanishAddress] when with all inputs considered, the selection is considered a final address selection
 * since there is no further selections that need to be made by the user.
 */
class GetFinalDanishAddressSelectionUseCase(
  private val getDanishAddressAutoCompletionUseCase: GetDanishAddressAutoCompletionUseCase,
) {
  suspend operator fun invoke(
    selectedAddress: DanishAddress,
    lastSelection: DanishAddress?,
  ): FinalAddressResult {
    // Exit fast if it's not a valid selection anyway
    if (selectedAddress.isValidFinalSelection.not()) return FinalAddressResult.NotFinalAddress
    // Exit fast with the result if the address selected can't possible have a more detailed version of it exist
    if (selectedAddress.hasAllProperties) return FinalAddressResult.Found(selectedAddress)
    // Return if the same address was selected as in the previous selection
    if (lastSelection?.isSameAddressAs(selectedAddress) == true) return FinalAddressResult.Found(selectedAddress)

    // Find if the selected address is unique
    val newResults: List<DanishAddress> = fetchNewResults(selectedAddress).getOrElse {
      return FinalAddressResult.NetworkError
    }
    if (newResults.size == 1) {
      if (newResults.first().isSameAddressAs(selectedAddress)) return FinalAddressResult.Found(selectedAddress)
    }
    return FinalAddressResult.NotFinalAddress
  }

  private suspend fun fetchNewResults(address: DanishAddress): Either<OperationResult.Error, List<DanishAddress>> {
    return getDanishAddressAutoCompletionUseCase
      .invoke(address, AddressAutocompleteType.APARTMENT)
      .map(AddressAutoCompleteResults::resultList)
  }
}

sealed interface FinalAddressResult {
  object NetworkError : FinalAddressResult
  object NotFinalAddress : FinalAddressResult
  data class Found(val address: DanishAddress) : FinalAddressResult
}
