package com.hedvig.android.feature.purchase.pet.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.PetAvailableBreedsQuery
import octopus.type.PriceIntentAnimal

internal interface GetPetBreedsUseCase {
  suspend fun invoke(animal: PriceIntentAnimal): Either<ErrorMessage, List<Breed>>
}

internal class GetPetBreedsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetPetBreedsUseCase {
  override suspend fun invoke(animal: PriceIntentAnimal): Either<ErrorMessage, List<Breed>> {
    return either {
      apolloClient
        .query(PetAvailableBreedsQuery(animal = animal))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to fetch pet breeds: $it" }
            raise(ErrorMessage())
          },
          ifRight = { data ->
            data.priceIntentAvailableBreeds.map { breed ->
              Breed(
                id = breed.id,
                displayName = breed.displayName,
                isMixedBreed = breed.isMixedBreed,
              )
            }
          },
        )
    }
  }
}
