package com.hedvig.android.feature.movingflow.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationViewModel
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleViewModel
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressViewModel
import com.hedvig.android.feature.movingflow.ui.start.StartViewModel
import com.hedvig.android.feature.movingflow.ui.summary.SummaryViewModel
import com.hedvig.android.featureflags.FeatureManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val movingFlowModule = module {
  single<MovingFlowStorage> {
    MovingFlowStorage(get<DataStore<Preferences>>())
  }
  single<MovingFlowRepository> {
    MovingFlowRepository(get<MovingFlowStorage>())
  }
  viewModel<StartViewModel> {
    StartViewModel(
      get<ApolloClient>(),
      get<MovingFlowRepository>(),
    )
  }
  viewModel<EnterNewAddressViewModel> {
    EnterNewAddressViewModel(
      savedStateHandle = get<SavedStateHandle>(),
      movingFlowRepository = get<MovingFlowRepository>(),
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }
  viewModel<AddHouseInformationViewModel> {
    AddHouseInformationViewModel(
      savedStateHandle = get<SavedStateHandle>(),
      movingFlowRepository = get<MovingFlowRepository>(),
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }
  viewModel<ChoseCoverageLevelAndDeductibleViewModel> {
    ChoseCoverageLevelAndDeductibleViewModel(get<MovingFlowRepository>())
  }
  viewModel<SummaryViewModel> {
    SummaryViewModel(
      get(),
      get(),
      get(),
      get(),
    )
  }
}
