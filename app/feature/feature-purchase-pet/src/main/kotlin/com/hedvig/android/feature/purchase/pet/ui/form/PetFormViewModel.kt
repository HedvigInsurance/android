package com.hedvig.android.feature.purchase.pet.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.pet.data.Breed
import com.hedvig.android.feature.purchase.pet.data.CreatePetSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.pet.data.GetPetBreedsUseCase
import com.hedvig.android.feature.purchase.pet.data.PRODUCT_NAME_CAT
import com.hedvig.android.feature.purchase.pet.data.PetGender
import com.hedvig.android.feature.purchase.pet.data.PetOffers
import com.hedvig.android.feature.purchase.pet.data.SessionAndIntent
import com.hedvig.android.feature.purchase.pet.data.SubmitInput
import com.hedvig.android.feature.purchase.pet.data.SubmitPetFormAndGetOffersUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import octopus.type.PriceIntentAnimal

internal class PetFormViewModel(
  productName: String,
  createPetSessionAndPriceIntentUseCase: CreatePetSessionAndPriceIntentUseCase,
  getPetBreedsUseCase: GetPetBreedsUseCase,
  submitPetFormAndGetOffersUseCase: SubmitPetFormAndGetOffersUseCase,
) : MoleculeViewModel<PetFormEvent, PetFormState>(
    initialState = PetFormState(),
    presenter = PetFormPresenter(
      productName = productName,
      createPetSessionAndPriceIntentUseCase = createPetSessionAndPriceIntentUseCase,
      getPetBreedsUseCase = getPetBreedsUseCase,
      submitPetFormAndGetOffersUseCase = submitPetFormAndGetOffersUseCase,
    ),
  )

internal sealed interface PetFormEvent {
  data class SubmitForm(
    val name: String,
    val breed: Breed?,
    val birthDate: LocalDate?,
    val gender: PetGender?,
    val isNeutered: Boolean?,
    val speciesAnswer: Boolean?,
    val street: String,
    val zipCode: String,
  ) : PetFormEvent

  data object ClearNavigation : PetFormEvent

  data object Retry : PetFormEvent

  data object DismissError : PetFormEvent
}

internal data class PetFormState(
  val productName: String = "",
  val isCat: Boolean = false,
  val isLoadingSession: Boolean = true,
  val loadSessionError: Boolean = false,
  val breeds: List<Breed> = emptyList(),
  val isSubmitting: Boolean = false,
  val submitError: String? = null,
  val nameError: String? = null,
  val breedError: String? = null,
  val birthDateError: String? = null,
  val genderError: String? = null,
  val isNeuteredError: String? = null,
  val speciesAnswerError: String? = null,
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val offersToNavigate: OffersNavigationData? = null,
)

internal data class OffersNavigationData(
  val shopSessionId: String,
  val offers: PetOffers,
)

private class PetFormPresenter(
  private val productName: String,
  private val createPetSessionAndPriceIntentUseCase: CreatePetSessionAndPriceIntentUseCase,
  private val getPetBreedsUseCase: GetPetBreedsUseCase,
  private val submitPetFormAndGetOffersUseCase: SubmitPetFormAndGetOffersUseCase,
) : MoleculePresenter<PetFormEvent, PetFormState> {
  @Composable
  override fun MoleculePresenterScope<PetFormEvent>.present(lastState: PetFormState): PetFormState {
    val isCat = productName == PRODUCT_NAME_CAT
    var currentState by remember {
      mutableStateOf(lastState.copy(productName = productName, isCat = isCat))
    }
    var sessionAndIntent: SessionAndIntent? by remember { mutableStateOf(null) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var pendingSubmit: PetFormEvent.SubmitForm? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is PetFormEvent.SubmitForm -> {
          val errors = validate(event, isCat)
          currentState = currentState.copy(
            nameError = errors.nameError,
            breedError = errors.breedError,
            birthDateError = errors.birthDateError,
            genderError = errors.genderError,
            isNeuteredError = errors.isNeuteredError,
            speciesAnswerError = errors.speciesAnswerError,
            streetError = errors.streetError,
            zipCodeError = errors.zipCodeError,
          )
          if (!errors.hasErrors()) {
            pendingSubmit = event
            submitIteration++
          }
        }

        PetFormEvent.ClearNavigation -> {
          currentState = currentState.copy(offersToNavigate = null)
        }

        PetFormEvent.Retry -> {
          if (sessionAndIntent == null) {
            currentState = currentState.copy(loadSessionError = false, isLoadingSession = true)
            loadIteration++
          } else {
            currentState = currentState.copy(submitError = null)
          }
        }

        PetFormEvent.DismissError -> {
          currentState = currentState.copy(submitError = null)
        }
      }
    }

    LaunchedEffect(loadIteration) {
      currentState = currentState.copy(isLoadingSession = true, loadSessionError = false)
      val animal = if (isCat) PriceIntentAnimal.CAT else PriceIntentAnimal.DOG
      val results = coroutineScope {
        val sessionDeferred = async { createPetSessionAndPriceIntentUseCase.invoke(productName) }
        val breedsDeferred = async { getPetBreedsUseCase.invoke(animal) }
        listOf(sessionDeferred, breedsDeferred).awaitAll()
      }
      val sessionResult = results[0] as arrow.core.Either<*, *>
      val breedsResult = results[1] as arrow.core.Either<*, *>

      val session = sessionResult.fold(ifLeft = { null }, ifRight = { it as SessionAndIntent })

      @Suppress("UNCHECKED_CAST")
      val breeds = breedsResult.fold(ifLeft = { null }, ifRight = { it as List<Breed> })

      if (session != null && breeds != null) {
        sessionAndIntent = session
        currentState = currentState.copy(
          breeds = breeds,
          isLoadingSession = false,
          loadSessionError = false,
        )
      } else {
        currentState = currentState.copy(isLoadingSession = false, loadSessionError = true)
      }
    }

    LaunchedEffect(submitIteration) {
      val submit = pendingSubmit ?: return@LaunchedEffect
      val session = sessionAndIntent ?: return@LaunchedEffect
      val breed = submit.breed ?: return@LaunchedEffect
      val birthDate = submit.birthDate ?: return@LaunchedEffect
      val gender = submit.gender ?: return@LaunchedEffect
      val isNeutered = submit.isNeutered ?: return@LaunchedEffect
      val speciesAnswer = submit.speciesAnswer ?: return@LaunchedEffect
      pendingSubmit = null
      currentState = currentState.copy(isSubmitting = true, submitError = null)

      submitPetFormAndGetOffersUseCase.invoke(
        SubmitInput(
          priceIntentId = session.priceIntentId,
          productName = productName,
          ssn = session.ssn,
          email = session.email,
          name = submit.name.trim(),
          breedId = breed.id,
          isMixedBreed = breed.isMixedBreed,
          birthDate = birthDate,
          gender = gender,
          isNeutered = isNeutered,
          speciesAnswer = speciesAnswer,
          street = submit.street.trim(),
          zipCode = submit.zipCode.trim(),
        ),
      ).fold(
        ifLeft = { error ->
          currentState = currentState.copy(
            isSubmitting = false,
            submitError = error.message ?: "Something went wrong",
          )
        },
        ifRight = { offers ->
          currentState = currentState.copy(
            isSubmitting = false,
            offersToNavigate = OffersNavigationData(
              shopSessionId = session.shopSessionId,
              offers = offers,
            ),
          )
        },
      )
    }

    return currentState
  }
}

private data class ValidationErrors(
  val nameError: String?,
  val breedError: String?,
  val birthDateError: String?,
  val genderError: String?,
  val isNeuteredError: String?,
  val speciesAnswerError: String?,
  val streetError: String?,
  val zipCodeError: String?,
) {
  fun hasErrors(): Boolean = listOf(
    nameError,
    breedError,
    birthDateError,
    genderError,
    isNeuteredError,
    speciesAnswerError,
    streetError,
    zipCodeError,
  ).any { it != null }
}

private fun validate(event: PetFormEvent.SubmitForm, isCat: Boolean): ValidationErrors {
  // TODO: Add "Enter a name" / "Ange ett namn" to Lokalise
  val nameError = if (event.name.isBlank()) "Enter a name" else null
  // TODO: Add "Choose a breed" / "Välj en ras" to Lokalise
  val breedError = if (event.breed == null) "Choose a breed" else null
  // TODO: Add "Choose a birth date" / "Välj födelsedatum" to Lokalise
  val birthDateError = if (event.birthDate == null) "Choose a birth date" else null
  // TODO: Add "Choose a gender" / "Välj kön" to Lokalise
  val genderError = if (event.gender == null) "Choose a gender" else null
  // TODO: Add "Answer this question" / "Besvara frågan" to Lokalise
  val isNeuteredError = if (event.isNeutered == null) "Answer this question" else null
  val speciesAnswerError = if (event.speciesAnswer == null) "Answer this question" else null
  // TODO: Add "Enter an address" / "Ange en adress" to Lokalise
  val streetError = if (event.street.isBlank()) "Enter an address" else null
  // TODO: Add "Enter a valid zip code (5 digits)" / "Ange ett giltigt postnummer (5 siffror)" to Lokalise
  val zipCodeError = when {
    event.zipCode.length != 5 -> "Enter a valid zip code (5 digits)"
    !event.zipCode.all { it.isDigit() } -> "Enter a valid zip code (5 digits)"
    else -> null
  }
  return ValidationErrors(
    nameError,
    breedError,
    birthDateError,
    genderError,
    isNeuteredError,
    speciesAnswerError,
    streetError,
    zipCodeError,
  )
}
