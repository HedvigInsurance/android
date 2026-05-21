package com.hedvig.android.feature.purchase.pet.di

import com.hedvig.android.feature.purchase.pet.data.CreatePetSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.pet.data.CreatePetSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.pet.data.GetPetBreedsUseCase
import com.hedvig.android.feature.purchase.pet.data.GetPetBreedsUseCaseImpl
import com.hedvig.android.feature.purchase.pet.data.SubmitPetFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.pet.data.SubmitPetFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.pet.ui.form.PetFormViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val petPurchaseModule = module {
  single<CreatePetSessionAndPriceIntentUseCase> { CreatePetSessionAndPriceIntentUseCaseImpl(apolloClient = get()) }
  single<GetPetBreedsUseCase> { GetPetBreedsUseCaseImpl(apolloClient = get()) }
  single<SubmitPetFormAndGetOffersUseCase> { SubmitPetFormAndGetOffersUseCaseImpl(apolloClient = get()) }

  viewModel<PetFormViewModel> { params ->
    PetFormViewModel(
      productName = params.get(),
      createPetSessionAndPriceIntentUseCase = get(),
      getPetBreedsUseCase = get(),
      submitPetFormAndGetOffersUseCase = get(),
    )
  }
}
