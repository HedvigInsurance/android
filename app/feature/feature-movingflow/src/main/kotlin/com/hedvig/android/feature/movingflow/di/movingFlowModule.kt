package com.hedvig.android.feature.movingflow.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleViewModel
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressViewModel
import com.hedvig.android.feature.movingflow.ui.start.StartViewModel
import com.hedvig.android.feature.movingflow.ui.summary.SummaryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val movingFlowModule = module {
  single<MovingFlowStorage> {
    MovingFlowStorage(get<DataStore<Preferences>>())
  }
  viewModel<StartViewModel> {
    StartViewModel(
      get<ApolloClient>(),
      get<MovingFlowStorage>(),
    )
  }
  viewModel<EnterNewAddressViewModel> {
    EnterNewAddressViewModel(
      get<SavedStateHandle>(),
      get<MovingFlowStorage>(),
      get<ApolloClient>(),
    )
  }
  viewModel<ChoseCoverageLevelAndDeductibleViewModel> {
    ChoseCoverageLevelAndDeductibleViewModel(get<MovingFlowStorage>())
  }
  viewModel<SummaryViewModel> {
    SummaryViewModel(
      get(),
      get(),
      get(),
    )
  }
}
