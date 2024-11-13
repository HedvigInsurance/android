package com.hedvig.android.feature.changeaddress.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManager
import com.hedvig.android.feature.changeaddress.data.ChangeAddressRepository
import com.hedvig.android.feature.changeaddress.data.NetworkChangeAddressRepository
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressViewModel
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationViewModel
import com.hedvig.android.feature.changeaddress.destination.offer.ChangeAddressOfferViewModel
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.SelectHousingTypeViewModel
import com.hedvig.android.feature.changeaddress.navigation.MovingParameters
import com.hedvig.android.feature.changeaddress.navigation.SelectHousingTypeParameters
import com.hedvig.android.language.LanguageService
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val changeAddressModule = module {
  single<ChangeAddressRepository> {
    NetworkChangeAddressRepository(
      get<ApolloClient>(),
      get<SelfServiceCompletedEventManager>(),
    )
  }

  viewModel<SelectHousingTypeViewModel> {
    SelectHousingTypeViewModel(
      changeAddressRepository = get<ChangeAddressRepository>(),
    )
  }
  viewModel<EnterNewAddressViewModel> { params ->
    EnterNewAddressViewModel(
      languageService = get<LanguageService>(),
      previousParameters = params.get<SelectHousingTypeParameters>(),
    )
  }
  viewModel<EnterVillaInformationViewModel> { params ->
    EnterVillaInformationViewModel(
      previousParameters = params.get<MovingParameters>(),
    )
  }

  viewModel<ChangeAddressOfferViewModel> { params ->
    ChangeAddressOfferViewModel(
      changeAddressRepository = get<ChangeAddressRepository>(),
      previousParameters = params.get<MovingParameters>(),
    )
  }
}
